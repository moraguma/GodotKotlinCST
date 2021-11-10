package com.example.game

import com.example.game.cst.AgentMind
import com.example.game.environment.Apple
import com.example.game.environment.Player
import godot.Node
import godot.annotation.RegisterClass
import godot.core.Vector2

@RegisterClass
class CollectorMindCommunicator: Node() {
    lateinit var mind: AgentMind
    lateinit var player: Player

    companion object {
        lateinit var instance: CollectorMindCommunicator
    }

    init {
        instance = this

        mind = AgentMind()
    }

    /*
        Agent Requests
    */

    fun getPosition(): Vector2 {
        return player.getPos()
    }

    fun getApplesInReach(): ArrayList<Apple> {
        return player.getApplesInTouch()
    }

    fun getApplesInVision(): ArrayList<Apple> {
        return player.getApplesInVision()
    }

    fun eatApple(code: String) {
        player.eatApple(code)
    }

    fun forage() {
        player.forage()
    }

    fun goTo(pos: Vector2) {
        player.goTo(pos)
    }
}