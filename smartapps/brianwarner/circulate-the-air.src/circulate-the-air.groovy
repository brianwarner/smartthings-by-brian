/**
 *  Circulate the air
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
    name: "Circulate the air",
    namespace: "brianwarner",
    author: "Brian Warner",
    description: "Periodically turn on a thermostat&#39;s fan to circulate air.",
    category: "Green Living",
    iconUrl: "http://cdn.device-icons.smartthings.com/Appliances/appliances11-icn.png",
    iconX2Url: "http://cdn.device-icons.smartthings.com/Appliances/appliances11-icn@2x.png",
    iconX3Url: "http://cdn.device-icons.smartthings.com/Appliances/appliances11-icn@4x.png")


preferences {
	section() {
		input "thermostats", "capability.thermostat", required: true, multiple: true, title: "For these thermostats:"
	}
	section() {
    	input "runTime","number", required: true, title: "Run the fan for this many minutes:"
    }
    section () {
    	input "runFreq","enum", options: ["1 hour", "3 hours"], title: "Every:"
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

	if (runFreq == "1 hour") {
		runEvery1Hour(turnFanOn)
		log.debug("Scheduling fan to run every hour")
	} else {
    	runEvery3Hours(turnFanOn)
        log.debug("Scheduling fan to run every 3 hours")
    }
}

def turnFanOn() {
	log.debug("Turning on thermostat fan")
	thermostats.fanOn()
    runIn(60*runTime,setFanAuto)
}

def setFanAuto() {
	log.debug("Setting thermostat fan to auto")
    thermostats.fanAuto()
}