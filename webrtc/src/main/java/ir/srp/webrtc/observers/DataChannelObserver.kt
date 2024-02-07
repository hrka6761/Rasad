package ir.srp.webrtc.observers

import ir.srp.webrtc.utils.ChannelEventsListener
import org.webrtc.DataChannel

class DataChannelObserver(
    private val eventsListener: ChannelEventsListener?
) : DataChannel.Observer {
    override fun onBufferedAmountChange(p0: Long) {
        // Not yet implemented
    }

    override fun onStateChange() {
        // Not yet implemented
    }

    override fun onMessage(buffer: DataChannel.Buffer?) {
        eventsListener?.onReceiveChannelData(buffer)
    }
}