import os
import json
from requests import get

import sys
sys.path.insert(0, "ieml")

from ieml.ieml_database import GitInterface, IEMLDatabase
from ieml.usl.word import Word
from ieml.usl.usl import usl

OUTPUT_FILENAME="resources/words_sample.json"


resource_dir = os.path.dirname(OUTPUT_FILENAME)
if not os.path.isdir(resource_dir):
    os.mkdir(resource_dir)

def get_word_structure(w: Word):
    return get("https://dev.intlekt.io/api/words/{}/?repository=IEMLdev".format(str(w))).json()

gitdb = GitInterface()
gitdb.pull() # download database in ~/.cache/ieml/ folder

# instanciate a ieml.ieml_database.IEMLDatabase from the downloaded git repository
db = IEMLDatabase(folder=gitdb.folder)

usls = db.list(parse=True, type='word')

parsed_usls = [get_word_structure(e) for e in usls]

with open(OUTPUT_FILENAME, "w") as fout:
    json.dump(parsed_usls, fout, indent=2)
