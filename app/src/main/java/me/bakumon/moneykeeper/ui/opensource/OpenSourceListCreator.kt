package me.bakumon.moneykeeper.ui.opensource

import java.util.*

/**
 * 开源库列表数据生成器
 *
 * @author Bakumon https://bakumon.me
 */
object OpenSourceListCreator {

    val openSourceList: ArrayList<OpenSourceBean>
        get() {
            val list = ArrayList<OpenSourceBean>()
            list.add(OpenSourceBean("android support libraries - Google",
                    "https://source.android.com",
                    "Apache Software License 2.0"))
            list.add(OpenSourceBean("android arch lifecycle - Google",
                    "https://source.android.com",
                    "Apache Software License 2.0"))
            list.add(OpenSourceBean("android arch room - Google",
                    "https://source.android.com",
                    "Apache Software License 2.0"))
            list.add(OpenSourceBean("RxJava - ReactiveX",
                    "https://github.com/ReactiveX/RxJava",
                    "Apache Software License 2.0"))
            list.add(OpenSourceBean("RxAndroid - ReactiveX",
                    "https://github.com/ReactiveX/rxAndroid",
                    "Apache Software License 2.0"))
            list.add(OpenSourceBean("retrofit - square",
                    "https://github.com/square/retrofit",
                    "Apache Software License 2.0"))
            list.add(OpenSourceBean("okhttp - square",
                    "https://github.com/square/okhttp",
                    "Apache Software License 2.0"))
            list.add(OpenSourceBean("leakcanary - square",
                    "https://github.com/square/leakcanary",
                    "Apache Software License 2.0"))
            list.add(OpenSourceBean("BRVAH - CymChad",
                    "https://github.com/CymChad/BaseRecyclerViewAdapterHelper",
                    "Apache Software License 2.0"))
            list.add(OpenSourceBean("MPAndroidChart - PhilJay",
                    "https://github.com/PhilJay/MPAndroidChart",
                    "Apache Software License 2.0"))
            list.add(OpenSourceBean("floo - drakeet",
                    "https://github.com/drakeet/Floo",
                    "Apache Software License 2.0"))
            list.add(OpenSourceBean("StatusLayoutManager - Bakumon",
                    "https://github.com/Bakumon/StatusLayoutManager",
                    "The MIT License"))
            list.add(OpenSourceBean("easypermissions - googlesamples",
                    "https://github.com/googlesamples/easypermissions",
                    "Apache Software License 2.0"))
            list.add(OpenSourceBean("android-storage - sromku",
                    "https://github.com/sromku/android-storage",
                    "Apache Software License 2.0"))
            list.add(OpenSourceBean("MaterialDateTimePicker - wdullaer",
                    "https://github.com/wdullaer/MaterialDateTimePicker",
                    "Apache Software License 2.0"))
            list.add(OpenSourceBean("pager-layoutmanager - GcsSloop",
                    "https://github.com/GcsSloop/pager-layoutmanager",
                    "Apache Software License 2.0"))
            list.add(OpenSourceBean("LayoutManagerGroup - DingMouRen",
                    "https://github.com/DingMouRen/LayoutManagerGroup",
                    "Apache Software License 2.0"))
            list.add(OpenSourceBean("AlipayZeroSdk - fython",
                    "https://github.com/fython/AlipayZeroSdk",
                    "Apache Software License 2.0"))
            list.add(OpenSourceBean("prettytime - ocpsoft",
                    "https://github.com/ocpsoft/prettytime",
                    "Apache Software License 2.0"))
            list.add(OpenSourceBean("CircleImageView - hdodenhof",
                    "https://github.com/hdodenhof/CircleImageView",
                    "Apache Software License 2.0"))
            list.add(OpenSourceBean("material-dialogs - afollestad",
                    "https://github.com/afollestad/material-dialogs",
                    "The MIT License"))
            list.add(OpenSourceBean("java-aes-crypto - tozny",
                    "https://github.com/tozny/java-aes-crypto",
                    "The MIT License"))
            list.add(OpenSourceBean("ProcessPhoenix - JakeWharton",
                    "https://github.com/JakeWharton/ProcessPhoenix",
                    "Apache Software License 2.0"))
            list.add(OpenSourceBean("okhttp-digest - rburgst",
                    "https://github.com/rburgst/okhttp-digest",
                    "Apache Software License 2.0"))
            list.add(OpenSourceBean("Cipher.so - MEiDIK",
                    "https://github.com/MEiDIK/Cipher.so",
                    "Apache Software License 2.0"))
            return list
        }
}
