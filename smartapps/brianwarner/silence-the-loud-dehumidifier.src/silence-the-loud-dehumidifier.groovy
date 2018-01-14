/**
 *  Silence the loud dehumidifier
 *
 *  Copyright 2016 Brian Warner
 *
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License. You may obtain a copy of the License at:
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
 *  on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License
 *  for the specific language governing permissions and limitations under the License.
 *
 */
definition(
    name: "Silence the loud dehumidifier",
    namespace: "brianwarner",
    author: "Brian Warner",
    description: "Humidity sensor triggers the dehumidifier, but only if nobody is in the room.",
    category: "Convenience",
    iconUrl: "http://cdn.device-icons.smartthings.com/Weather/weather12-icn.png",
    iconX2Url: "http://cdn.device-icons.smartthings.com/Weather/weather12-icn@2x.png",
    iconX3Url: "http://cdn.device-icons.smartthings.com/Weather/weather12-icn@3x.png")


preferences {
	section("Preferences") {
		input "theHumiditySensor", "capability.relativeHumidityMeasurement", title: "Which humidity sensor?", required: true
		input "dehumidifierSwitch", "capability.switch", title: "Which switch controls the dehumidifier?", required: true
		input "maxHumidity", "number", title: "Turn on at this humidity", required: true
		input "minHumidity", "number", title: "Turn off at this humidity", required: true

		input "theMotionSensor", "capability.motionSensor", title: "Which motion sensor?", required: true
		input "lockout", "number", title: "How long should the dehumidifier be paused when motion is detected?", required: true
	}
}

def installed() {
	log.debug "Installed with settings: ${settings}"

	initialize()
}

def updated() {
	log.debug "Updated with settings: ${settings}"

	unsubscribe()
	initialize()
}

def initialize() {
	subscribe(theHumiditySensor, "humidity", humidityHandler)
	subscribe(theMotionSensor, "motion.active", motionHandler)
	subscribe(theMotionSensor, "motion.inactive", motionStoppedHandler)
}

def humidityHandler(evt) {

	def motionState = theMotionSensor.currentState("motion")

	if (motionState.value == "inactive") {

		// Check to see if the sensor has been inactive long enough to consider turning on the dehumidifier
		def elapsed = now() - motionState.date.time
		def threshold = 1000 * 60 * lockout

		if (elapsed >= threshold) {

			// It's been long enough.  Checking to see if we need to dehumidify.
			def currentHumidity = theHumiditySensor.latestValue("humidity")

			if (currentHumidity > maxHumidity) {

				// It's humid enough.  Turn on the switch.
				dehumidifierSwitch.on()
			} else if (currentHumidity < minHumidity) {
	
				// It's dry enough.  Turn off the switch.
				dehumidifierSwitch.off()
			} 
		}
	}
}

def motionHandler(evt) {

	// Immediately turns the dehumidifer off when motion is detected
	dehumidifierSwitch.off()
}

def motionStoppedHandler(evt) {

	// When motion stops, check back after the lockout period to determine if we need to turn back on again.
    runIn(60 * lockout, humidityHandler)
}