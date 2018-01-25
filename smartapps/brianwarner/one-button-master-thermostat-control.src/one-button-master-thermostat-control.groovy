/**
 *  Turn thermostat fans on
 *
 *  Copyright 2017 Brian Warner
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
    name: "One button master thermostat control",
    namespace: "brianwarner",
    author: "Brian Warner",
    description: "One button control for all the thermostats in the house.  Create Momentary Button Tile devices for each action.",
    category: "Green Living",
    iconUrl: "http://cdn.device-icons.smartthings.com/Appliances/appliances11-icn.png",
    iconX2Url: "http://cdn.device-icons.smartthings.com/Appliances/appliances11-icn@2x.png",
    iconX3Url: "http://cdn.device-icons.smartthings.com/Appliances/appliances11-icn@4x.png")


preferences {
	section() {
		input "thermostats", "capability.thermostat", required: true, multiple: true, title: "For these thermostats:"
	}
	section() {
		input "switchFanOn", "capability.momentary", title: "Turn on the fans:"
	}
	section() {
		input "switchFanAuto", "capability.momentary", title: "Set fans to auto:"
	}
	section() {
		input "switchSystemOff", "capability.momentary", title: "Turn all thermostats off:"
	}
	section() {
		input "switchSystemAuto", "capability.momentary", title: "Set all thermostats to auto:"
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
	subscribe(switchFanOn,"momentary.pushed", fansOn)
	subscribe(switchFanAuto,"momentary.pushed", fansAuto)
	subscribe(switchSystemOff,"momentary.pushed", systemOff)
	subscribe(switchSystemAuto,"momentary.pushed", systemAuto)
}

def fansOn(evt) {
	sendNotificationEvent("Turning on all thermostat fans")
	thermostats?.fanOn()
}

def fansAuto(evt) {
	sendNotificationEvent("Setting all thermostat fans to auto")
	thermostats?.fanAuto()
}

def systemOff(evt) {
	sendNotificationEvent("Turning off all thermostats")
	thermostats?.off()
}

def systemAuto(evt) {
	sendNotificationEvent("Setting all thermostats to auto")
	thermostats?.auto()
}