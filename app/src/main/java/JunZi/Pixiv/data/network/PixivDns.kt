package JunZi.Pixiv.data.network

import okhttp3.Dns
import java.net.InetAddress
import java.net.UnknownHostException

class PixivDns : Dns {
    override fun lookup(hostname: String): List<InetAddress> {
        val mapped = PixivNetworkConfig.addressesFor(hostname)
        return when {
            mapped.isNotEmpty() -> mapped.flatMap { InetAddress.getAllByName(it).toList() }.distinct()
            hostname.isIpAddress() -> InetAddress.getAllByName(hostname).toList()
            PixivNetworkConfig.shouldUseCompatibilityClient() && PixivNetworkConfig.requiresDynamicRoute(hostname) -> {
                throw UnknownHostException("No dynamic IP for $hostname; waiting for API DNS route")
            }
            else -> Dns.SYSTEM.lookup(hostname)
        }
    }

    private fun String.isIpAddress(): Boolean {
        return matches(Regex("""\d{1,3}(\.\d{1,3}){3}"""))
    }

}
