package ir.syrent.velocityreport.bridge

interface Bridge {

    fun sendPluginMessage(sender: Any, messageByte: ByteArray)

    fun sendPluginMessage(messageByte: ByteArray)

}