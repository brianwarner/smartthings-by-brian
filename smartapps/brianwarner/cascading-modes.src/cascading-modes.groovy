/**
 *  Cascading modes
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
    name: "Cascading modes",
    namespace: "brianwarner",
    author: "Brian Warner",
    description: "If a certain mode is set at a certain time, switch to another.",
    category: "Mode Magic",
    iconUrl: "http://cdn.device-icons.smartthings.com/Office/office6-icn.png",
    iconX2Url: "http://cdn.device-icons.smartthings.com/Office/office6-icn@2x.png",
    iconX3Url: "http://cdn.device-icons.smartthings.com/Office/office6-icn@4x.png")


preferences {
	section() {
    	input "changeTime", "time", title: "At", required: true
    	input "targetMode", "mode", title: "Set the mode to", required: true
	}
}

def installed() {
	log.debug "Installed with settings: ${settings}"

	initialize()
}

def updated() {
	log.debug "Updated with settings: ${settings}"

	unsubscribe()
    unschedule()
	initialize()
}

def initialize() {
	schedule(changeTime,changeHandler)
}

def changeHandler(evt) {

	if (location.modes?.find{it.name == targetMode}) {
    	setLocationMode(targetMode)
        sendNotificationEvent("Changing the mode to ${targetMode}.")
    }
}