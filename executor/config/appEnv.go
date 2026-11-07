package config

import (
	"log"
	"os"
	"slices"

	"github.com/joho/godotenv"
)

var validProfiles = [1]string{"local"}

func LoadProfile() {
	appProfile := os.Getenv("EXECUTOR_PROFILE")

	if appProfile == "" || !slices.Contains(validProfiles[:], appProfile) {
		log.Fatal("Please pass along a valid application profile")
	}

	envFile := ".env." + appProfile
	err := godotenv.Load(envFile)
	if err != nil {
		log.Fatal("Error ocurred while loading env file: ", envFile)
	}
}
