/**
 *  You left the garage open
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
    name: "You left the garage open",
    namespace: "brianwarner",
    author: "Brian Warner",
    description: "Watchdog app that detects when you left the garage door open.  If you have a presence sensor, it will try to determine if you meant to leave it up (for example, so you could work outside) and will give you a bit more time before warning you.",
    category: "Convenience",
    iconUrl: "http://cdn.device-icons.smartthings.com/Transportation/transportation13-icn.png",
    iconX2Url: "http://cdn.device-icons.smartthings.com/Transportation/transportation13-icn@2x.png",
    iconX3Url: "http://cdn.device-icons.smartthings.com/Transportation/transportation13-icn@3x.png")

preferences {
	section() {
    	input "contactsensors", "capability.contactSensor", multiple: true, title: "When these doors open:"
	}

	section() {
    	input "timerAway", "number", required: true, title: "Alert me if they're still open after this many minutes:", range: "0..240"
	}

	section() {
    	input "timerAwayReminder", "number", required: true, title: "And keep reminding me after this many minutes until it closes:", range: "0..240"
    }

    section() {
    	input "presence", "capability.presenceSensor", required: false, multiple: true, title: "(Optional) Unless this presence sensor is here:"
    }
    
	section() {
    	input "timerPresent", "number", required: false, title: "In which case alert me after this many minutes:", range: "0..240"
	}

	section() {
    	input "timerPresentReminder", "number", required: false, title: "And keep reminding me after this many minutes until it closes:", range: "0..240"
    }
    
    section() {
        input("recipients", "contact", title: "Send notifications to:") {
            input "phone", "phone", title: "Send a text message:", description: "Phone Number", required: false
            input "push", "bool", title: "Send a push notification:", required: false
        }
    }
    section() {
		input "modes", "mode", title: "But don't warn me in these modes:", multiple: true
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
    if (presence) {
		log.debug "There's a presence sensor here, and its status just changed so reset the timer."
		subscribe(presence, "presence", doorOpenHandler)
    }
}

def doorOpenHandler(evt) {
	if (modes.contains(location.mode)) {
    	log.debug "Running in a disabled mode (${location.mode}), stop checking."
    } else {
		if (presence) {
            log.debug "A presence sensor is defined, so act accordingly if it is here or away."
            def anyPresenceSensorHere = presence.findAll {it.currentValue("presence") == "present"}

            if (anyPresenceSensorHere) {
                def onPresence = anyPresenceSensorHere.displayName
                log.debug "There's a presence sensor here, check if things are closed using the 'present' interval."
                runIn(15 * timerPresent, checkClosed)
            } else {
                log.debug "Presence sensor is away, check if things are closed using the 'not present' interval."
                runIn(15 * timerAway, checkClosed)
            }
        } else {
            log.debug "No presence sensors defined."
            runIn(15 * timerAway, checkClosed)
        }
    }
}

def checkClosed() {

	def openContactSensors = contactsensors.findAll {it.currentValue("contact") == "open"}

	log.debug "Checking if things are closed."

	if (openContactSensors) {
        def openNames = openContactSensors.displayName

		if (modes.contains(location.mode)) {
			log.debug "Someone changed the mode to one that suppresses warnings.  Leave a message in the log, and exit."
			sendNotificationEvent("You left these doors open, but asked not to be warned: ${openNames.join(', ')}")
		} else {
            log.debug "Timer expired, warn that the door is still open."

			def message = "These doors are still open, should they be? ${openNames.join(', ')}"

			if (location.contactBookEnabled && recipients) {
                sendNotificationToContacts(message, recipients)
            } else {
                if ((phone) && (push)) {
                    sendNotification(message, [method: "both", phone: phone])
                } else if (phone) {
                    sendNotification(message, [method: "phone", phone: phone])
                } else if (push) {
                    sendNotification(message, [method: "push"])
                }
            }

            def anyPresenceSensorHere = presence.findAll {it.currentValue("presence") == "present"}

            if (anyPresenceSensorHere) {
                log.debug "There's a presence sensor here, run using the present interval"
                runIn(15*timerPresentReminder,checkClosed)
            } else {
                log.debug "No presence sensor here, run using the not present interval"
                runIn(15*timerAwayReminder,checkClosed)
            }

            log.debug "Going to check back."
		}
	} else {
    	log.debug "Someone closed the door in time." 
    }
}
