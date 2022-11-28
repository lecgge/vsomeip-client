package com.example.vsomeip

import java.lang.reflect.Method

object DemoConfig {
    object NavigationWeaInfo {
        const val ServiceID = 0xB047
        const val InstanceID = 0x0001
        const val EventID1 = 0x0002
        const val EventID2 = 0x8002

        const val EventGroupID = 0x02
    }

    object NavigationTrailInfo {
        const val ServiceID = 0xB046
        const val InstanceID = 0x0001
        const val EventID = 0x01
        const val EventGroupID = 0x01

        const val MethodID = 0x01
    }

    object MAPDynInfo {
        const val ServiceID = 0xB048
        const val InstanceID = 0x0001
        const val EventID1 = 0x8003
        const val EventID2 = 0x8004
        const val EventID3 = 0x8005
        const val EventID4 = 0x8006

        const val EventGroupID = 0x03
    }

    object EgoPosInfo {
        const val ServiceID = 0xB049
        const val InstanceID = 0x0001
        const val EventID = 0x8007
        const val EventGroupID = 0x04
    }

    object ClientIDS {
        const val ClientID = 0x6789
    }

}