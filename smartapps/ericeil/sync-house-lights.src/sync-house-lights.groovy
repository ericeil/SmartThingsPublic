/**
 *  Sync aquarium with Noon at night
 *
 *  Copyright 2018 Eric Eilebrecht
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
    name: "Sync house lights",
    namespace: "ericeil",
    author: "Eric Eilebrecht",
    description: "Keep three Noon switches in sync, and keep my aquarium light in sync with the living room lights",
    category: "My Apps",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png",
    iconX3Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png")


preferences {
    section {
        input(name: "dayMode", type: "mode", title: "Day Mode", required: true)
        input(name: "eveMode", type: "mode", title: "Evening Mode", required: true)
        input(name: "aquarium", type: "capability.switchLevel", title: "Aquarium", required: true)
        input(name: "triggers", type: "capability.momentary", title: "Buttons to respond to Noon", multiple: true, required: true)
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
	log.debug "$aquarium.currentSwitch, $aquarium.currentLevel"
	if (aquarium.currentSwitch == "on")
		state.aquariumLevel = aquarium.currentLevel
    else
    	state.aquariumLevel = 0
        
    log.debug "Starting aquarium level: $state.aquariumLevel"

	subscribe(triggers, "switch.on", triggerHandler)
	subscribe(location, "mode", modeHandler)
}

def triggerHandler(evt) {

	def dev = evt.getDevice().displayName
	log.debug "Received event: $dev"	

	switch(dev)
    {
  	case "Main Floor set to Bright":
    	state.aquariumLevel = 100
        break
  	case "Main Floor set to Normal":
  	case "Main Floor set to TV":
  	case "Main Floor set to Kitchen":
  	case "Main Floor set to Dining":
    	state.aquariumLevel = 50
        break

	case "Main Floor set to Dim":
    	state.aquariumLevel = 13
        break

	case "Main Floor set to Off":
    	state.aquariumLevel = 0
        break

    }

	log.debug "New aquarium level: $state.aquariumLevel"
    adjustAquarium(location.mode)
}

def modeHandler(evt) {
	log.debug "New mode: $location.mode";	
    adjustAquarium(evt.value)
}

def adjustAquarium(def mode) {
	log.debug "Current mode: $location.currentMode dayMode: $dayMode eveMode: $eveMode"
    if (mode == dayMode && state.aquariumLevel != 0)
    {
    	log.debug "Would have set aquarium to $state.aquariumLevel, but it's daytime. Turning aquarium on 100%"
        aquarium.setLevel(100);
        aquarium.on();
    }
    else if (mode == eveMode)
    {
		log.debug "Setting aquarium to $state.aquariumLevel"
        if (state.aquariumLevel == 0)
        {
            aquarium.off()
        }
        else
        {
            aquarium.setLevel(state.aquariumLevel);
            aquarium.on()
        }
    }
}