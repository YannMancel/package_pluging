FLUTTER_VERSION?=3.19.5
FLUTTER?= fvm flutter
FOLDER?= projects
PROJECT?= project
DESCRIPTION?= description

GREEN_COLOR=\033[32m
NO_COLOR=\033[0m

define print_color_message
	@echo "$(GREEN_COLOR)$(1)$(NO_COLOR)";
endef

##
## -------------------------------------------------------------------------------------------------
## Setup
## -------------------------------------------------------------------------------------------------
##

.PHONY: install
install: ## Install environment
	@$(call print_color_message,"Install environment")
	fvm install $(FLUTTER_VERSION)
	fvm use $(FLUTTER_VERSION)

##
## -------------------------------------------------------------------------------------------------
## Flutter
## -------------------------------------------------------------------------------------------------
##

.PHONY: generate-plugin
generate-plugin: ## Generate a plugin
	# make generate-plugin PROJECT=battery_info DESCRIPTION="A Flutter plugin to manage battery info."
	@$(call print_color_message,"Generate a plugin")
	$(FLUTTER) create ./$(FOLDER)/$(PROJECT)/ \
		--description="$(DESCRIPTION)" \
		--template=plugin \
      	--platforms=android \
      	--org="com.mancel.yann" \
	  	--project-name=$(PROJECT)

##
## -------------------------------------------------------------------------------------------------
## scrcpy
## -------------------------------------------------------------------------------------------------
##

.PHONY: mirror
mirror: ## Mirror screen with scrcpy
	@$(call print_color_message,"Mirror screen with scrcpy")
	scrcpy --max-size 1024 --window-title 'My device'

.PHONY: record
record: ## Record screen with scrcpy
	@$(call print_color_message,"Record screen with scrcpy")
	scrcpy --max-size 1024 --no-display --record "flutter_$(shell date +%Y%m%d-%H%M%S).mp4"

#
# --------------------------------------------------------------------------------------------------
# Help
# --------------------------------------------------------------------------------------------------
#

.DEFAULT_GOAL := help
.PHONY: help
help:
	@grep -E '(^[a-zA-Z_-]+:.*?##.*$$)|(^##)' $(MAKEFILE_LIST) | awk 'BEGIN {FS = ":.*?## "}; {printf "$(GREEN_COLOR)%-30s$(NO_COLOR) %s\n", $$1, $$2}' | sed -e 's/\[32m##/[33m/'