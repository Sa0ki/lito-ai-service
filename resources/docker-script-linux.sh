#!/usr/bin/bash

if [ -z "$1" ]; then
    # Assign a default name with current date and time
    fileName="default_$(date +"%Y-%m-%d_%H-%M-%S")"
else
    fileName="$1"
fi

input_path="/home/skinan/project/docker_input/${fileName}_input"  # Chemin vers le répertoire d'entrée sur le host
output_path="/home/skinan/project/docker_output/${fileName}_output/$fileName"  # Chemin vers le répertoire de sortie sur le host

echo "$input_path"
echo "$output_path"

echo "DOCKER IMAGE IS RUNNING..."
start=$(date +%s)

docker run -d -v "$input_path":/home/docker_input kibromberihu/mipsegmentator:latest-0

container_id=$(docker ps -q -l)

echo "WAITING FOR CONTAINER TO FINISH..."
docker wait $container_id

echo "COPYING RESULTS TO THE HOST..."
docker cp $container_id:/home/docker_output/. "$output_path"
echo "RESULTS ARE AVAILABLE."

echo "DISPLAYING CONTAINER LOGS"
docker logs $container_id

end=$(date +%s)
runtime=$((end-start))  # Calculer le temps d'exécution en secondes
echo "SCRIPT EXECUTION TIME: $runtime seconds"

echo "DELETING THE CONTAINER $container_id ..."
docker rm $container_id


