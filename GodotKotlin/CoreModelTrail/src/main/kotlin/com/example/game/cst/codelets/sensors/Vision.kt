package com.example.game.cst.codelets.sensors

import br.unicamp.cst.core.entities.Codelet
import br.unicamp.cst.core.entities.MemoryObject
import com.example.game.CollectorMindCommunicator

class Vision : Codelet() {
    /*
        Requests the apples in sight every proc. The body
        should respond to this request by updating the
        visionMO through the plugin's interface
    */

    private var visionMO: MemoryObject? = null

    override fun accessMemoryObjects() {
        visionMO = getInput("VISION") as MemoryObject?
    }

    override fun proc() {
        visionMO?.setI(CollectorMindCommunicator.instance.getApplesInVision())
    }

    override fun calculateActivation() {}
}