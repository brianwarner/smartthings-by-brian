/**
 *  Heat cable control
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
    name: "Protect the heat cables",
    namespace: "brianwarner",
    author: "Brian Warner",
    description: "Shut off heat cables based upon the outside temperature.",
    category: "Green Living",
    iconUrl: "http://cdn.device-icons.smartthings.com/Seasonal Winter/seasonal-winter-006-icn.png",
    iconX2Url: "http://cdn.device-icons.smartthings.com/Seasonal Winter/seasonal-winter-006-icn@2x.png",
    iconX3Url: "http://cdn.device-icons.smartthings.com/Seasonal Winter/seasonal-winter-006-icn@4x.png")


preferences {
	section("Preferences") {
		input "heatCables", "capability.switch", title: "Turn off these heat cables", required: true, multiple: true
        input "temperatureSensor", "capability.temperatureMeasurement", title: "When this temperature sensor", required: true
        input "turnOffTemp", "number", title: "Reports a temperature above", required: true, range: "0..50"
	}

    section("Send a text message to this number") {
        input "phone", "phone", required: true
    }
}

def installed() {
	log.debug "Installed with settings: ${settings}"

	unschedule()
	initialize()
}

def updated() {
	log.debug "Updated with settings: ${settings}"

	unsubscribe()
	initialize()
}

def initialize() {
	subscribe(temperatureSensor,"temperature",temperatureHandler)
    subscribe(heatCables,"switch.on",temperatureHandler)
}

def temperatureHandler(evt) {
	def heatCablesOn = heatCables.findAll { it?.latestValue("switch") == 'on' }

	if (heatCablesOn) {
    	def currentTemperature = temperatureSensor.latestValue("temperature")

		if (currentTemperature > turnOffTemp) {
        	heatCables.off()
            sendNotificationEvent("It's too warm, so I turned off the heat cables.")
			sendSms(phone, "It's too warm, so I turned off the heat cables.")
		}
    }
}
