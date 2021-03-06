/**
 *  Garage furnace control
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
    name: "Garage furnace control",
    namespace: "brianwarner",
    author: "Brian Warner",
    description: "Locks out a furnace when by shutting off the control switch when a door is opened.",
    category: "Green Living",
    iconUrl: "http://cdn.device-icons.smartthings.com/Home/home1-icn.png",
    iconX2Url: "http://cdn.device-icons.smartthings.com/Home/home1-icn@2x.png",
    iconX3Url: "http://cdn.device-icons.smartthings.com/Home/home1-icn@4x.png")


preferences {
	section() {
		input "doors", "capability.contactSensor", required: true, multiple: true, title: "When one of these doors is open:"
	}

	section() {
		input "furnaceSwitch", "capability.switch", required: true, multiple: false, title: "Shut off the furnace controlled by this switch:"
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
	subscribe(doors,"contact",doorHandler)
}

def doorHandler(evt) {
	if (evt.value == "open") {
        log.debug "A door opened, turning off furnace."
		furnaceSwitch.off()
    } else {
		def contactSensorState = doors.currentState("contact")
		def anyDoorsOpen = contactSensorState.value.findAll {it == "open"}
		if (!anyDoorsOpen) {
			log.debug "No more doors open, turning on furnace."
			furnaceSwitch.on()
		} else {
			log.debug "Still at least one door open, keeping furnace off."        
        }
	}
}
