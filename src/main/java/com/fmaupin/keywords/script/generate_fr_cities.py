##
## Copyright (C) 2025 Fabrice MAUPIN
##
## This file is part of Extract Micro Service.
##
##  Read Content Micro Service is free software: you can redistribute it and/or modify
## it under the terms of the GNU General Public License version 3,
## as published by the Free Software Foundation.
##
## This program is distributed in the hope that it will be useful,
## but WITHOUT ANY WARRANTY; without even the implied warranty of
## MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
## GNU General Public License for more details.
##
## You should have received a copy of the GNU General Public License
## along with this program. If not, see <https://www.gnu.org/licenses/>.

##
## generate_fr_cities
##
## Script pour générer la liste des communes de france.
##
## @author Fabrice MAUPIN
## @version 0.0.1-SNAPSHOT
## @since 05/11/25

import csv
import requests
import sys

# URL de base pour les fichiers data.gouv.fr (la resource_id sera ajoutée)
BASE_URL = "https://www.data.gouv.fr/fr/datasets/r/"

# Destination du fichier de sortie
OUTPUT_FILE = "src/main/resources/fr_cities.txt"

def download_and_extract_cities(resource_id: str, output_path: str):
    if not resource_id:
        raise ValueError("You must provide valid resource_id of data.gouv.fr")

    data_url = f"{BASE_URL}{resource_id}"
    print(f"Download file from : {data_url}")

    response = requests.get(data_url)
    response.raise_for_status()

    text = response.content.decode("utf-8", errors="replace")
    reader = csv.DictReader(text.splitlines(), delimiter=';')
    cities = set()

    for row in reader:
        # Selon la colonne disponible
        name = (row.get('Nom_commune') or row.get('Nom_de_la_commune') or '').strip()
        if name:
            cities.add(name)

    print(f"{len(cities)} cities extracted (duplicates removed).")

    # Tri alphabétique et écriture dans le fichier
    with open(output_path, "w", encoding="utf-8") as f:
        for city in sorted(cities):
            f.write(city + "\n")

    print(f"File generated : {output_path}")

if __name__ == "__main__":
    if len(sys.argv) < 2:
        print("Usage: python generate_fr_cities.py <resource_id_data_gouv>")
        sys.exit(1)

    resource_id = sys.argv[1]
    download_and_extract_cities(resource_id, OUTPUT_FILE)
