package com.example.vsomeip

import android.os.Bundle
import android.system.ErrnoException
import android.system.Os
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.example.vsomeip.VSomeIPClient.Companion.TAG
import com.example.vsomeip.databinding.ActivityMainBinding
import org.json.JSONObject
import java.io.BufferedReader
import java.io.File
import java.io.FileOutputStream
import java.io.InputStreamReader
import java.net.InetAddress
import java.net.NetworkInterface
import java.util.*

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        CrashReport.initCrashReport(getApplicationContext(), "注册时申请的APPID", false);

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        Log.d("vsomeip-test", "client-Address: ${getIPAddress(true)}")
        init_vsomeip()

        var vSomeIPClient = VSomeIPClient("Client", DemoClientListener())
        var b = vSomeIPClient.initialize()
        if (vSomeIPClient.initialize()) {

            //NavigationTrailInfo
            vSomeIPClient.requestService(
                DemoConfig.NavigationTrailInfo.ServiceID,
                DemoConfig.NavigationTrailInfo.InstanceID
            )
            vSomeIPClient.requestEvent(
                DemoConfig.NavigationTrailInfo.ServiceID,
                DemoConfig.NavigationTrailInfo.InstanceID,
                DemoConfig.NavigationTrailInfo.EventID,
                DemoConfig.NavigationTrailInfo.EventGroupID,
                true
            )
            vSomeIPClient.subscribeEventGroup(
                DemoConfig.NavigationTrailInfo.ServiceID,
                DemoConfig.NavigationTrailInfo.InstanceID,
                DemoConfig.NavigationTrailInfo.EventGroupID
            )

            //NavigationWeaInfo
            vSomeIPClient.requestService(
                DemoConfig.NavigationWeaInfo.ServiceID,
                DemoConfig.NavigationWeaInfo.InstanceID
            )
            vSomeIPClient.requestEvent(
                DemoConfig.NavigationWeaInfo.ServiceID,
                DemoConfig.NavigationWeaInfo.InstanceID,
                DemoConfig.NavigationWeaInfo.EventID2,
                DemoConfig.NavigationWeaInfo.EventGroupID,
                false
            )
            vSomeIPClient.requestEvent(
                DemoConfig.NavigationWeaInfo.ServiceID,
                DemoConfig.NavigationWeaInfo.InstanceID,
                DemoConfig.NavigationWeaInfo.EventID1,
                DemoConfig.NavigationWeaInfo.EventGroupID,
                false
            )
            vSomeIPClient.subscribeEventGroup(
                DemoConfig.NavigationWeaInfo.ServiceID,
                DemoConfig.NavigationWeaInfo.InstanceID,
                DemoConfig.NavigationWeaInfo.EventGroupID,
            )
            //MAPDynInfo
            vSomeIPClient.requestService(
                DemoConfig.MAPDynInfo.ServiceID,
                DemoConfig.MAPDynInfo.InstanceID
            )
            vSomeIPClient.requestEvent(
                DemoConfig.MAPDynInfo.ServiceID,
                DemoConfig.MAPDynInfo.InstanceID,
                DemoConfig.MAPDynInfo.EventID1,
                DemoConfig.MAPDynInfo.EventGroupID,
                false
            )
            vSomeIPClient.requestEvent(
                DemoConfig.MAPDynInfo.ServiceID,
                DemoConfig.MAPDynInfo.InstanceID,
                DemoConfig.MAPDynInfo.EventID2,
                DemoConfig.MAPDynInfo.EventGroupID,
                false
            )
            vSomeIPClient.requestEvent(
                DemoConfig.MAPDynInfo.ServiceID,
                DemoConfig.MAPDynInfo.InstanceID,
                DemoConfig.MAPDynInfo.EventID3,
                DemoConfig.MAPDynInfo.EventGroupID,
                false
            )
            vSomeIPClient.requestEvent(
                DemoConfig.MAPDynInfo.ServiceID,
                DemoConfig.MAPDynInfo.InstanceID,
                DemoConfig.MAPDynInfo.EventID4,
                DemoConfig.MAPDynInfo.EventGroupID,
                false
            )
            vSomeIPClient.subscribeEventGroup(
                DemoConfig.MAPDynInfo.ServiceID,
                DemoConfig.MAPDynInfo.InstanceID,
                DemoConfig.MAPDynInfo.EventGroupID,
            )

            //EgoPosInfo
            vSomeIPClient.requestService(
                DemoConfig.EgoPosInfo.ServiceID,
                DemoConfig.EgoPosInfo.InstanceID
            )
            vSomeIPClient.requestEvent(
                DemoConfig.EgoPosInfo.ServiceID,
                DemoConfig.EgoPosInfo.InstanceID,
                DemoConfig.EgoPosInfo.EventID,
                DemoConfig.EgoPosInfo.EventGroupID,
                false
            )
            vSomeIPClient.subscribeEventGroup(
                DemoConfig.EgoPosInfo.ServiceID,
                DemoConfig.EgoPosInfo.InstanceID,
                DemoConfig.EgoPosInfo.EventGroupID
            )

            Thread { vSomeIPClient.startClient() }.start()

            Log.d(TAG, "SomeIP Client start...")

            binding.sampleText.setOnClickListener {
                vSomeIPClient.sendRequest(
                    DemoConfig.NavigationTrailInfo.ServiceID,
                    DemoConfig.NavigationTrailInfo.InstanceID,
                    DemoConfig.NavigationTrailInfo.MethodID,
                    byteArrayOf(0x00, 0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07, 0x08, 0x09),
                    true
                )
//                vSomeIPClient.sendRequest(
//                    DemoConfig.NavigationWeaInfo.ServiceID,
//                    DemoConfig.NavigationWeaInfo.InstanceID,
//                    0x1002,
//                    byteArrayOf(0x00, 0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07, 0x08, 0x09),
//                    false
//                )
            }
        } else {
            Log.d(TAG, "init someip client failed!")
        }
    }


    companion object {
        const val JSONFILENAME: String = "vsomeip-client.json"
    }

    private fun init_vsomeip() {
        val someipConfig = File(cacheDir, JSONFILENAME)
        try {
            if (someipConfig.exists()) {
                someipConfig.delete()
            }
            someipConfig.createNewFile()
            writeConfigToFile(someipConfig)
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
        try {
            Os.setenv(
                "VSOMEIP_CONFIGURATION",
                applicationContext.cacheDir.path + "/" + JSONFILENAME,
                true
            )
            Os.setenv("VSOMEIP_BASE_PATH", applicationContext.cacheDir.path + "/", true)
            Os.setenv("VSOMEIP_APPLICATION_NAME", "Client", true)
        } catch (e: ErrnoException) {
            e.printStackTrace()
        }
    }

    @Throws(Exception::class)
    private fun writeConfigToFile(file: File) {
        val fileOutputStream = FileOutputStream(file)
        var value = getJson(JSONFILENAME)
        val jsonObject = JSONObject(value)
        jsonObject.put("unicast", getIPAddress(true))
        Log.d("vsomeip-test", "client-Address: ${getIPAddress(true)}")
        value = jsonObject.toString()
        Log.d("vsomeip-test", "writeConfigToFile: ${value}")
        val bytes = value.toByteArray()
        fileOutputStream.write(bytes)
        fileOutputStream.close()
    }

    fun getIPAddress(useIPv4: Boolean): String? {
        try {
            val interfaces: List<NetworkInterface> =
                Collections.list(NetworkInterface.getNetworkInterfaces())
            for (intf in interfaces) {
                val addrs: List<InetAddress> = Collections.list(intf.inetAddresses)
                for (addr in addrs) {
                    if (!addr.isLoopbackAddress) {
                        val sAddr = addr.hostAddress
                        //boolean isIPv4 = InetAddressUtils.isIPv4Address(sAddr);
                        val isIPv4 = sAddr.indexOf(':') < 0
                        if (useIPv4) {
                            if (isIPv4) return sAddr
                        } else {
                            if (!isIPv4) {
                                val delim = sAddr.indexOf('%') // drop ip6 zone suffix
                                return if (delim < 0) sAddr.uppercase(Locale.getDefault()) else sAddr.substring(
                                    0,
                                    delim
                                ).uppercase(
                                    Locale.getDefault()
                                )
                            }
                        }
                    }
                }
            }
        } catch (ignored: Exception) {
        } // for now eat exceptions
        return ""
    }

    private fun getJson(fileName: String): String {
        val stringBuffer = StringBuffer()
        val assetManager = assets
        try {
            val reader = BufferedReader(InputStreamReader(assetManager.open(fileName)))
            var line: String?
            while (reader.readLine().also { line = it } != null) {
                stringBuffer.append(line)
            }
        } catch (e: Exception) {
        }
        return stringBuffer.toString()
    }
}