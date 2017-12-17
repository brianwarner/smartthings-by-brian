/**
 *  Presence Sensing Mode Changer
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
    name: "Presence Sensing Mode Changer",
    namespace: "brianwarner",
    author: "Brian Warner",
    description: "Set more complicated rules for mode changes, such as when some people are here, and others aren&#39;t.  In addition, do this on a schedule.",
    category: "Mode Magic",
    iconUrl: "http://cdn.device-icons.smartthings.com/Home/home4-icn.png",
    iconX2Url: "http://cdn.device-icons.smartthings.com/Home/home4-icn@2x.png",
    iconX3Url: "http://cdn.device-icons.smartthings.com/Home/home4-icn@4x.png")


preferences {
	section("Primary group") {
    	input "primaryLogic", "enum", title: "When", options: ["Any","All"], required: true
    	input "primaryMembers", "capability.presenceSensor", title: "Of these people", required: true, multiple: true
    	input "primaryStatus", "enum", title: "Are", options: ["Here","Away"], required: true
    }
    
    section ("Secondary group (optional)") {
    	input "secondaryLogic", "enum", title: "And when", options: ["Any","All"], hideWhenEmpty: "secondGroupControl"
    	input "secondaryMembers", "capability.presenceSensor", title: "Of these people", multiple: true, hideWhenEmpty: "secondGroupControl"
    	input "secondaryStatus", "enum", title: "Are", options: ["Here","Away"], hideWhenEmpty: "secondGroupControl"
    }
    
    section("Mode") {
    	input "targetMode", "mode", title: "Set the mode to", required: true
    }
    
    section("Schedule") {
    	input "fromTime", "time", title: "Starting at", required: true
    	input "toTime", "time", title: "And ending at", required: true
    	input "days", "enum", title: "On these days (all by default)", required: false, multiple: true, options: ["Monday": "Monday", "Tuesday": "Tuesday", "Wednesday": "Wednesday", "Thursday": "Thursday", "Friday": "Friday"]
    	input "excludeModes", "mode", title: "Unless this mode is set", multiple: true
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
	subscribe(primaryMembers, "presence", presenceHandler)
	subscribe(secondaryMembers, "presence", presenceHandler)
    schedule(fromTime,presenceHandler)
}

def presenceHandler(evt) {
	// First, check to see if the user defined specific days for this.   
	if (days) {
    	// Thanks to SmartThings documentation for the following code to check days/times.
		def df = new java.text.SimpleDateFormat("EEEE")

		// Ensure the new date object is set to local time zone
    	df.setTimeZone(location.timeZone)
    	def day = df.format(new Date())

		//Does the preference input Days, i.e., days-of-week, contain today?
    	def dayCheck = days.contains(day)
    	if (dayCheck) {
			checkTime()
        }
	} else {
    	checkTime()
    }
}

def checkTime() {
	// Next, check if the time is right.
	def between = timeOfDayIsBetween(fromTime, toTime, new Date(), location.timeZone)
    if (between) {
		checkPrimaryPeople()
	}
}

def checkPrimaryPeople() {
	// Next, find out if the primary people condition is met.
	def primaryPresenceSensorState = primaryMembers.currentState("presence")
	def primarySensorsHere = primaryPresenceSensorState.value.findAll {it == "present"}

	if (primaryStatus == "Here") {
		if (primarySensorsHere.size() == primaryMembers.size()) {
			log.debug "Primary: Everybody is here"
        	checkSecondaryPeople()
		} else if ((primaryLogic == "Any") && (primarySensorsHere.size() > 0)) {
        	log.debug "Primary: Somebody is here"
            checkSecondaryPeople()
        }
    } else {
    	if (primarySensorsHere.size() == 0) {
        	log.debug "Primary: Nobody is here"
            checkSecondaryPeople()
        } else if ((primaryLogic == "Any") && (primarySensorsHere.size() < primaryMembers.size())) {
        	log.debug "Primary: Somebody is gone"
            checkSecondaryPeople()
        }
    }
}

def checkSecondaryPeople() {
	// Next, find out if the secondary people condition is met.
	if (secondaryMembers.size() > 0) {

		def secondaryPresenceSensorState = secondaryMembers.currentState("presence")
		def secondarySensorsHere = secondaryPresenceSensorState.value.findAll {it == "present"}

		if (secondaryStatus == "Here") {
			if (secondarySensorsHere.size() == secondaryMembers.size()) {
				log.debug "Secondary: Everybody is here"
	        	changeMode()
			} else if ((secondaryLogic == "Any") && (secondarySensorsHere.size() > 0)) {
    	    	log.debug "Secondary: Somebody is here"
	            changeMode()
        	}
    	} else {
	    	if (secondarySensorsHere.size() == 0) {
        		log.debug "Secondary: Nobody is here"
    	        changeMode()
	        } else if ((secondaryLogic == "Any") && (secondarySensorsHere.size() < secondaryMembers.size())) {
        		log.debug "Secondary: Somebody is gone"
        	    changeMode()
    	    }
	    }
    } else {
		log.debug "No secondary members set."
		changeMode()
    }
}

def changeMode() {
	// If all looks good, try to change the mode.
	if (location.mode != targetMode) {
    	if (location.modes?.find{it.name == targetMode}) {
            setLocationMode(targetMode)
            sendNotificationEvent("Changing mode to ${targetMode}")
        }  else {
            log.warn "Tried to change to undefined mode '${targetMode}'"
        }
    } else {
    	log.debug "'${targetMode}' was already set."
    }
}