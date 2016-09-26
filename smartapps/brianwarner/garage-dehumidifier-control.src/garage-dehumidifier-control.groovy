/**
 *  Garage dehumidifier control
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
    name: "Garage dehumidifier control",
    namespace: "brianwarner",
    author: "Brian Warner",
    description: "Only run a dehumidifier when the humidity is high and when all of the garage doors are closed.",
    category: "Green Living",
    iconUrl: "http://cdn.device-icons.smartthings.com/Weather/weather12-icn.png",
    iconX2Url: "http://cdn.device-icons.smartthings.com/Weather/weather12-icn@2x.png",
    iconX3Url: "http://cdn.device-icons.smartthings.com/Weather/weather12-icn@4x.png")


preferences {
	section() {
		input "dehumidifierSwitch", "capability.switch", required: true, multiple: false, title: "Run the dehumidifier on this switch:"
	}

	section() {
		input "humiditySensor", "capability.relativeHumidityMeasurement", required: true, multiple: false, title: "When this humidity sensor:"
	}

	section() {
		input "dehumidifierOn", "number", required: true, multiple: false, title: "Detects humidity above this level:"
	}
    
	section() {
		input "dehumidifierOff", "number", required: true, multiple: false, title: "Until humidity is below this level:"
	}
    
	section() {
		input "doors", "capability.contactSensor", required: true, multiple: true, title: "But only when these doors are all closed:"
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
	subscribe(humiditySensor,"humidity",checkHumidity)
	subscribe(doors,"contact",doorHandler)
}

def checkHumidity(evt) {
    def humidity = humiditySensor.latestValue("humidity")
	if (humidity >= dehumidifierOn) {
		def contactSensorState = doors.currentState("contact")
		def anyDoorsOpen = contactSensorState.value.findAll {it == "open"}
		if (!anyDoorsOpen) {
            log.debug "Humidity is high (${humidity}), turning on."
            dehumidifierSwitch.on()
		} else {
    		log.debug "Humidity is high (${humidity}), but a door was open so doing nothing."
    	}
	} else if (humidity < dehumidifierOff) {
    	log.debug "Humidity is back in normal range (${humidity}), turning off."    
		dehumidifierSwitch.off()
    } else {
    	log.debug "Humidity is in the correct range (${humidity})."
    }
}

def doorHandler(evt) {
	if (evt.value == "open") {
        log.debug "A door opened, turning off."
		dehumidifierSwitch.off()
    } else {
		checkHumidity(NULL)
	}
}
