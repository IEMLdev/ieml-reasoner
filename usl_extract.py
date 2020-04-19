import os
import json
from requests import get
from tqdm import tqdm
import bz2

import sys
sys.path.insert(0, "ieml")

from ieml.ieml_database import GitInterface, IEMLDatabase
from ieml.usl.word import Word
from ieml.usl.usl import usl

WORDS_FILENAME="resources/words_sample.json"
DICTIONARY_FILENAME="resources/dictionary.json"


resource_dir = "resources"
if not os.path.isdir(resource_dir):
    os.mkdir(resource_dir)

def get_word_structure(w: Word):
    return get("https://dev.intlekt.io/api/words/{}/?repository=IEMLdev".format(str(w))).json()

gitdb = GitInterface(origin="https://github.com/plevyieml/ieml-language")
gitdb.pull() # download database in ~/.cache/ieml/ folder

# instanciate a ieml.ieml_database.IEMLDatabase from the downloaded git repository
db = IEMLDatabase(folder=gitdb.folder)

usls = db.list(parse=True, type='word')

parsed_usls = list()
for e in tqdm(usls):
    parsed_usls.append(get_word_structure(e))

with bz2.open(WORDS_FILENAME + ".bz2", "wt") as fout:
    json.dump(parsed_usls, fout, indent=2)


descriptors = db.get_descriptors()

usls = db.list()

translations = list()
for e in tqdm(usls):
    assert(e not in translations)
    tr_dict = dict()
    values = descriptors.get_values_partial(e)
    for (usl, lang, label), tr_list in values.items():
        assert(usl == e)
        if label == "translations":
            assert(lang not in tr_dict)
            tr_dict[lang] = tr_list
    translations.append({"usl": e, "translations": tr_dict})

with bz2.open(DICTIONARY_FILENAME + ".bz2", "wt") as fout:
    json.dump(translations, fout, indent=2)
