#!/usr/bin/env python3
"""Structural reachability only: assumes Vanilla resources and new world drops are available."""
import json
from pathlib import Path

root = Path(__file__).resolve().parents[1]
recipes = {path.stem: json.loads(path.read_text()) for path in
           (root / 'src/main/resources/data/badspell/recipe').glob('*.json')}
reachable = {'badspell:' + value for value in [
    'raw_field_crystal', 'raw_arcane_ore', 'runewood_log', 'runewood_sapling',
    'runewood_leaves', 'moon_blossom', 'glow_moss', 'resonance_shard',
    'field_crystal_ore', 'arcane_ore', 'resonance_crystal']}


def available(ingredient):
    if isinstance(ingredient, str):
        return ingredient.startswith(('minecraft:', '#minecraft:')) or ingredient in reachable
    if isinstance(ingredient, list):
        return any(map(available, ingredient))
    if isinstance(ingredient, dict):
        # Components constrain the Vanilla potion; their correctness needs a Minecraft codec/test.
        return available(ingredient.get('items', ingredient.get('item', ingredient.get('ingredient', ''))))
    return False


pending = dict(recipes)
while True:
    previous = len(reachable)
    for key, recipe in list(pending.items()):
        inputs = recipe.get('ingredients', list(recipe.get('key', {}).values()))
        if 'ingredient' in recipe:
            inputs = [recipe['ingredient']]
        if 'inputs' in recipe:
            inputs = [entry['ingredient'] for entry in recipe['inputs']] + ['badspell:' + recipe['station']]
        if all(map(available, inputs)):
            reachable.update(output['id'] for output in recipe.get('outputs', [recipe['result']] if 'result' in recipe else []))
            del pending[key]
    # Snapshot engraving is an executable station operation, not a JSON recipe.
    # This proves only material reachability; SIGN/IMPULSE graph requirements
    # and owner/energy validation are covered by server tests, not this graph.
    for suffix in ('', '_q2', '_q3'):
        if all(item in reachable for item in ('badspell:engraving_station',
                'badspell:formula_book', 'badspell:blank_rune_plate' + suffix)):
            reachable.add('badspell:inscribed_rune_plate' + suffix)
    if len(reachable) == previous:
        break
if pending:
    raise SystemExit('Unreachable recipes: ' + ', '.join(sorted(pending)))
print(f'{len(recipes)} recipes structurally reachable; quantities, world generation and progression are not simulated.')
