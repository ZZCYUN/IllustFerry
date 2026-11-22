// Package illusttunnel 提供 ECH / 无 SNI 直连隧道。
//
// 通过 gomobile bind 生成 Android AAR（libillusttunnel.so + Java 绑定类），
// JNI 细节（String/[]byte 编组、线程 attach、JNIEnv 获取）全部由 gomobile
// 官方绑定层处理，避免手写 JNI 表调用。
//
//   - echB64 非空 → TCP + TLS 1.3 + ECH（外层 SNI = ECHConfig 的 public_name，
//     内层 SNI = serverName，完整证书链与主机名校验）
//   - echB64 为空 → TCP + TLS 1.3 无 SNI（ClientHello 不携带 SNI 扩展，适用于
//     按 IP 直连且无法预知证书域名的场景；握手后按证书 SAN 补主机名校验）
package illusttunnel

import (
	"context"
	"crypto/tls"
	"encoding/base64"
	"fmt"
	"net"
	"sync"
	"sync/atomic"
	"time"
)

type tunnel struct {
	conn   net.Conn
	err    atomic.Value
	closed atomic.Bool
}

var (
	tunnelsMu sync.Mutex
	tunnels   = make(map[int64]*tunnel)
	tunnelSeq int64
)

func lookup(id int64) *tunnel {
	tunnelsMu.Lock()
	defer tunnelsMu.Unlock()
	return tunnels[id]
}

func setErr(t *tunnel, format string, args ...interface{}) {
	t.err.Store(fmt.Sprintf(format, args...))
}

// Open 建立一条 TLS 隧道，返回句柄供 Read/Write/Close 使用。
//
// ip 为目标 IP（由 DoH 解析得到），serverName 为真实域名（ECH 模式下作为内层
// SNI 与证书校验名；无 SNI 模式下用于握手后的 SAN 校验），echB64 为
// cloudflare-ech.com HTTPS RR 下发的 ECHConfigList，空串表示无 SNI 模式。
func Open(ip string, serverName string, echB64 string, port int) (int64, error) {
	cfg, err := buildTLSConfig(serverName, echB64)
	if err != nil {
		return 0, fmt.Errorf("bad ech config: %w", err)
	}

	dialer := &net.Dialer{Timeout: 12 * time.Second}
	raw, err := dialer.DialContext(context.Background(), "tcp", net.JoinHostPort(ip, fmt.Sprintf("%d", port)))
	if err != nil {
		return 0, fmt.Errorf("dial %s:%d: %w", ip, port, err)
	}
	conn := tls.Client(raw, cfg)
	_ = conn.SetDeadline(time.Now().Add(15 * time.Second))
	if err := conn.Handshake(); err != nil {
		_ = raw.Close()
		return 0, fmt.Errorf("tls handshake %s via %s: %w", serverName, ip, err)
	}
	_ = conn.SetDeadline(time.Time{})

	// 无 SNI 模式下补 SAN 校验（InsecureSkipVerify 会跳过主机名校验）。
	if echB64 == "" {
		if !verifySAN(conn.ConnectionState(), serverName) {
			_ = raw.Close()
			return 0, fmt.Errorf("certificate SAN does not cover %s", serverName)
		}
	}

	id := atomic.AddInt64(&tunnelSeq, 1)
	tunnelsMu.Lock()
	tunnels[id] = &tunnel{conn: conn}
	tunnelsMu.Unlock()
	return id, nil
}

// Write 向隧道写入 data，返回写入字节数。
func Write(handle int64, data []byte) (int, error) {
	t := lookup(handle)
	if t == nil {
		return 0, fmt.Errorf("tunnel %d not found", handle)
	}
	n, err := t.conn.Write(data)
	if err != nil {
		setErr(t, "write: %v", err)
		return n, err
	}
	return n, nil
}

// Read 从隧道读取数据填充 buf，阻塞直到有数据、对端关闭或出错。
// 返回 0 且无错误表示对端已关闭。
func Read(handle int64, buf []byte) (int, error) {
	t := lookup(handle)
	if t == nil {
		return 0, fmt.Errorf("tunnel %d not found", handle)
	}
	if len(buf) == 0 {
		return 0, nil
	}
	n, err := t.conn.Read(buf)
	if err != nil {
		if t.closed.Load() {
			return 0, nil
		}
		setErr(t, "read: %v", err)
		return n, err
	}
	return n, nil
}

// Close 关闭并释放隧道。
func Close(handle int64) {
	tunnelsMu.Lock()
	t := tunnels[handle]
	delete(tunnels, handle)
	tunnelsMu.Unlock()
	if t == nil {
		return
	}
	t.closed.Store(true)
	_ = t.conn.Close()
}

// LastError 返回隧道最近一次 IO 错误的可读描述。
func LastError(handle int64) string {
	t := lookup(handle)
	if t == nil {
		return ""
	}
	if v, ok := t.err.Load().(string); ok {
		return v
	}
	return ""
}

func buildTLSConfig(serverName, echB64 string) (*tls.Config, error) {
	cfg := &tls.Config{
		MinVersion: tls.VersionTLS13,
		NextProtos: []string{"http/1.1"},
	}
	if echB64 == "" {
		// 无 SNI 模式：按 IP 直连，证书主机名校验由 verifySAN 事后补。
		cfg.InsecureSkipVerify = true
		cfg.ServerName = ""
		return cfg, nil
	}
	raw, err := base64.StdEncoding.DecodeString(echB64)
	if err != nil || len(raw) == 0 {
		return nil, fmt.Errorf("decode ech config: %w", err)
	}
	cfg.ServerName = serverName
	cfg.EncryptedClientHelloConfigList = raw
	return cfg, nil
}

func verifySAN(state tls.ConnectionState, serverName string) bool {
	if len(state.PeerCertificates) == 0 || serverName == "" {
		return false
	}
	leaf := state.PeerCertificates[0]
	return leaf.VerifyHostname(serverName) == nil
}
