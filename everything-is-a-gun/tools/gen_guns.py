#!/usr/bin/env python3
"""Override selected *vanilla* item definitions with GUI/held model splits.

The item remains minecraft:feather, minecraft:dirt, etc. GUI keeps its native
vanilla model; all non-GUI contexts use an EIAG 3-D gun made from its texture.
"""
import json
import os

ROOT = os.path.dirname(os.path.abspath(__file__))
RESOURCES = os.path.join(ROOT, "..", "src", "main", "resources", "assets")
EIAG = os.path.join(RESOURCES, "eiag")
VANILLA = os.path.join(RESOURCES, "minecraft", "items")

# (vanilla item-definition id, its ordinary model, texture used by the sculpture)
ITEMS = [
    ("dirt", "block/dirt", "block/dirt"), ("stone", "block/stone", "block/stone"),
    ("cobblestone", "block/cobblestone", "block/cobblestone"), ("oak_planks", "block/oak_planks", "block/oak_planks"),
    ("sand", "block/sand", "block/sand"), ("gravel", "block/gravel", "block/gravel"),
    ("glass", "block/glass", "block/glass"), ("obsidian", "block/obsidian", "block/obsidian"),
    ("tnt", "block/tnt", "block/tnt_side"), ("coal_block", "block/coal_block", "block/coal_block"),
    ("redstone_block", "block/redstone_block", "block/redstone_block"), ("lapis_block", "block/lapis_block", "block/lapis_block"),
    ("iron_block", "block/iron_block", "block/iron_block"), ("gold_block", "block/gold_block", "block/gold_block"),
    ("diamond_block", "block/diamond_block", "block/diamond_block"), ("emerald_block", "block/emerald_block", "block/emerald_block"),
    ("netherite_block", "block/netherite_block", "block/netherite_block"), ("iron_ingot", "item/iron_ingot", "item/iron_ingot"),
    ("gold_ingot", "item/gold_ingot", "item/gold_ingot"), ("diamond", "item/diamond", "item/diamond"),
    ("emerald", "item/emerald", "item/emerald"), ("netherite_ingot", "item/netherite_ingot", "item/netherite_ingot"),
    ("chicken", "item/chicken", "item/chicken"), ("feather", "item/feather", "item/feather"),
]

SILHOUETTE = [
    "................", "................", "................", "................",
    "..##............", ".###............", ".###............", ".##############.",
    ".##.............", ".###############", "..#.############", "..##............",
    "...#............", "................", "................", "................",
]
FACES = {face: {"uv": [0, 0, 16, 16], "texture": "#0"}
         for face in ("north", "south", "east", "west", "up", "down")}


def held_model(texture):
    elements = []
    for image_y, row in enumerate(SILHOUETTE):
        for x, pixel in enumerate(row):
            if pixel == "#":
                y = 15 - image_y
                elements.append({"from": [x, y, 5], "to": [x + 1, y + 1, 11], "faces": FACES})
    return {"parent": "minecraft:block/block", "textures": {"0": "minecraft:" + texture, "particle": "minecraft:" + texture}, "elements": elements}


def main():
    os.makedirs(VANILLA, exist_ok=True)
    os.makedirs(os.path.join(EIAG, "models", "item"), exist_ok=True)
    for item_id, gui_model, texture in ITEMS:
        item_def = {"model": {"type": "minecraft:select", "property": "minecraft:display_context",
            "cases": [{"when": ["gui"], "model": {"type": "minecraft:model", "model": "minecraft:" + gui_model}}],
            "fallback": {"type": "minecraft:model", "model": "eiag:item/" + item_id + "_held"}}}
        with open(os.path.join(VANILLA, item_id + ".json"), "w") as out:
            json.dump(item_def, out, indent=2)
        with open(os.path.join(EIAG, "models", "item", item_id + "_held.json"), "w") as out:
            json.dump(held_model(texture), out, indent=2)
    print("generated", len(ITEMS), "vanilla item model overrides")

if __name__ == "__main__":
    main()
