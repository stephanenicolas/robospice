#Converts a RoboSpice sample into an ant project.
#This script can be used on all RoboSpice samples.

if [ $# -eq 0 ]
  then
    echo "No arguments supplied. Provide the name of the folder of a sample project."
fi

cd $1 
#create ant files for current project
android update project -p .
#use maven to copy dependencies to the libs-for-ant folder
mvn clean install -Pant
#move all maven dependencies to the libs folder for ant
mv libs-for-ant libs
#build the project with ant
ant clean debug install
#back to here
cd ..

