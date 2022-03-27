# `eflect` for `BERT` experiments

these are some bootstrapping scripts that will run each `BERT` variant across some data set. the experiments can be setup with:

```bash
pip install -m requirements.txt
./setup_models.sh
./smoke_test.sh
```

and run with:

```bash
./run_variants.sh "${DATA_SET}"
```

if you want to run only the best-fit variants, you can run:

```bash
./run_named_variants.sh "${DATA_SET}"
```
