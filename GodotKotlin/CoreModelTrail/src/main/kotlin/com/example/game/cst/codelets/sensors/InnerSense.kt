package com.example.game.cst.codelets.sensors

import br.unicamp.cst.core.entities.Codelet
import br.unicamp.cst.core.entities.MemoryObject
import com.example.game.CollectorMindCommunicator

class InnerSense: Codelet() {
    /*
        Requests the agent's position every proc. The body
        should respond to this request by updating the
        positionMO through the plugin's interface
    */

    private var positionMO: MemoryObject? = null

    fun InnerSense() {
    }

    override fun accessMemoryObjects() {
        positionMO = getInput("POSITION") as MemoryObject
    }

    override fun proc() {
        positionMO?.setI(CollectorMindCommunicator.instance.getPosition())
    }

    override fun calculateActivation() {}
}