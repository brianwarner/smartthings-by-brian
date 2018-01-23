/**
 *  Light Up The Night
 *
 *  Copyright 2018 Brian Warner
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
    name: "Light Up The Night",
    namespace: "brianwarner",
    author: "Brian Warner",
    description: "Turn on certain lights when a door opens at night, and turn them off a certain time after the last door closes.  Great for garage doors and driveway lights.",
    category: "Convenience",
    iconUrl: "http://cdn.device-icons.smartthings.com/Lighting/light9-icn.png",
    iconX2Url: "http://cdn.device-icons.smartthings.com/Lighting/light9-icn@2x.png",
    iconX3Url: "http://cdn.device-icons.smartthings.com/Lighting/light9-icn@3x.png")


preferences {

section() {
    	input "contactsensors", "capability.contactSensor", required: false, multiple: true, title: "When these doors open:"
	}

	section() {
    	input "locks", "capability.lock", required: false, multiple: true, title: "Or these locks:"
	}

    section() {
		input "lights", "capability.switch", required: true, multiple: true, title: "Turn on these lights:"
    }
    
   section() {
    	input "timer", "number", required: true, title: "Keep them on until all doors are closed for this many minutes:", range: "0..240"
	}
    
    section() {
    	input "sunriseoffset", "number", required: true, title: "Start turning on the lights this many minutes before sunset:", range: "0..360"
    }

    section() {
    	input "sunsetoffset", "number", required: true, title: "Stop turning on the lights this many minutes after sunrise:", range: "0..360"
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
	subscribe(contactsensors, "contact.open", doorOpenHandler)
	subscribe(locks, "lock.unlocked", doorOpenHandler)
	subscribe(contactsensors, "contact.closed", doorClosedHandler)
	subscribe(locks, "lock.locked", doorClosedHandler)
}

def doorOpenHandler(evt) {

	def now = new Date()
	def sunTime = getSunriseAndSunset(sunriseOffset: "00:$sunriseoffset", sunsetOffset: "-00:$sunsetoffset")

	if (now > sunTime.sunset || now < sunTime.sunrise) {
		lights.on()
    }
}

def doorClosedHandler(evt) {
	runIn(5 * (timer), checkClosed)
}

def checkClosed() {

	def anyContactSensorsOpen = contactsensors.findAll { it?.latestValue("contact") == 'open' }
	def anyLocksOpen = locks.findAll { it?.latestValue("lock") == 'unlocked' }
    
	if (!anyContactSensorsOpen && !anyLocksOpen) {
        lights.off()
	}
}