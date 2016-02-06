/**
 *  Open the garage if the car isn't already there
 *
 *  Copyright 2015 Brian Warner
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
    name: "Open the garage if the car isn't already there",
    namespace: "brianwarner",
    author: "Brian Warner",
    description: "Open the garage door when a phone or presence sensor arrives, but only if the car isn't already there.  Aware of the typical lag between when a phone arrives home and the presence sensor polling interval.",
    category: "Safety & Security",
    iconUrl: "http://cdn.device-icons.smartthings.com/Transportation/transportation13-icn.png",
    iconX2Url: "http://cdn.device-icons.smartthings.com/Transportation/transportation13-icn@2x.png",
    iconX3Url: "http://cdn.device-icons.smartthings.com/Transportation/transportation13-icn@4x.png")


preferences {
    section() {
   		input "presence", "capability.presenceSensor", required: true, title: "When this presence sensor arrives:"
    }

	section() {
    	input "garage", "capability.doorControl", required: true, title: "Open this garage door:"
    }

	section() {
    	input "vehicle", "capability.presenceSensor", required: true, title: "Unless this vehicle is already there:"
	}

	section() {
    	input "vehicletimer", "number", required: true, title: "And has been there for at least this many minutes:"
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
	subscribe(presence,"presence.present",presenceHandler)
}

def presenceHandler(evt) {

	def vehicleState = vehicle.currentState("presence")
	def elapsed = now() - vehicleState.date.time
    def threshold = 1000 * 60 * vehicletimer

    if (vehicleState.value == "not present") {
    	log.debug "${presence.label} just arrived and ${vehicle.label} was not already here, so I'm opening ${garage.label}"
        sendNotificationEvent("${presence.label} just arrived and ${vehicle.label} was not already here, so I'm opening ${garage.label}")
        garage.open()
    } else if ((vehicleState.value == "present") && (elapsed < threshold)) {
    	log.debug "${presence.label} just arrived and ${vehicle.label} arrived at the same time, so I'm opening ${garage.label}"
        sendNotificationEvent("${presence.label} just arrived and ${vehicle.label} arrived at the same time, so I'm opening ${garage.label}")
		garage.open()
    } else {
    	log.debug "${presence.label} just arrived but ${vehicle.label} was already here, so I'm keeping ${garage.label} shut."
    	sendNotificationEvent("${presence.label} just arrived but ${vehicle.label} was already here, so I'm keeping ${garage.label} shut.")
    }

}
