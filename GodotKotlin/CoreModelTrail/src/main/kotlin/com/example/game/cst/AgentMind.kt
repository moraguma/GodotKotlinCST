package com.example.game.cst

import br.unicamp.cst.core.entities.Codelet
import br.unicamp.cst.core.entities.Mind
import com.example.game.cst.codelets.behaviors.EatApple
import com.example.game.cst.codelets.behaviors.Forage
import com.example.game.cst.codelets.behaviors.GoToClosestApple
import com.example.game.cst.codelets.motor.HandActionCodelet
import com.example.game.cst.codelets.motor.LegActionCodelet
import com.example.game.cst.codelets.perception.AppleDetector
import com.example.game.cst.codelets.perception.ClosestAppleDetector
import com.example.game.cst.codelets.sensors.InnerSense
import com.example.game.cst.codelets.sensors.Touch
import com.example.game.cst.codelets.sensors.Vision
import com.example.game.environment.Apple

class AgentMind : Mind() {
    init {
        // Declare Memory Objects
        val touchMO = createMemoryObject("TOUCH", ArrayList<Apple>())
        val positionMO = createMemoryObject("POSITION", null)
        val visionMO = createMemoryObject("VISION", ArrayList<Apple>())
        val knownApplesMO = createMemoryObject("KNOWN_APPLES", ArrayList<Apple>())
        val closestAppleMO = createMemoryObject("CLOSEST_APPLE", null)
        val handsMO = createMemoryObject("HANDS", ArrayList<Apple>())
        val legsMO = createMemoryObject("LEGS", null)

        // Create Sensor Codelets
        val touch: Codelet = Touch()
        insertCodelet(touch, "SENSOR")
        val innerSense: Codelet = InnerSense()
        insertCodelet(innerSense, "SENSOR")
        val vision: Codelet = Vision()
        insertCodelet(vision, "SENSOR")

        // Create Perception Codelets
        val appleDetector: Codelet = AppleDetector()
        appleDetector.addInput(visionMO)
        appleDetector.addOutput(knownApplesMO)
        insertCodelet(appleDetector, "PERCEPTION")
        val closestAppleDetector: Codelet = ClosestAppleDetector()
        closestAppleDetector.addInput(positionMO)
        closestAppleDetector.addInput(knownApplesMO)
        closestAppleDetector.addOutput(closestAppleMO)
        insertCodelet(closestAppleDetector, "PERCEPTION")

        // Create Motor Codelets
        val handActionCodelet: Codelet = HandActionCodelet()
        handActionCodelet.addInput(handsMO)
        insertCodelet(handActionCodelet, "MOTOR")
        val legActionCodelet: Codelet = LegActionCodelet()
        legActionCodelet.addInput(legsMO)
        insertCodelet(legActionCodelet, "MOTOR")

        // Create Behavior Codelets
        val forage: Codelet = Forage()
        forage.addInput(knownApplesMO)
        forage.addOutput(legsMO)
        insertCodelet(forage, "BEHAVIOR")
        val eatApple: Codelet = EatApple()
        eatApple.addInput(touchMO)
        eatApple.addOutput(handsMO)
        insertCodelet(eatApple, "BEHAVIOR")
        val goToClosestApple: Codelet = GoToClosestApple()
        goToClosestApple.addInput(closestAppleMO)
        goToClosestApple.addOutput(legsMO)
        insertCodelet(goToClosestApple, "BEHAVIOR")

        // Start Cognitive Cycle
        start()
    }
}