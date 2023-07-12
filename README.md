# TokenMinerEnchant
NOTE: This a custom enchant module requires [TokenEnchant](https://polymart.org/resource/155) plugin.

This enchantment is provided as an example to show how you can make your own custom enchantmnet.  The function as a token miner is rather obsolete (although you can still use it).  The new enchantment called [CommandMiner](https://te.polymart.org/resource/1115) can be used to define TokenMiner enchantment.  Since CommandMiner is a polymorphic enchantment, you can define as many XXXMiner enchant from it.

This plugin contains a custom enchantment effect that allows you to gain tokens when you mine!.

It supports fortune enchant and explosive-type enchants.  You can adjust multiplier and also fortune multiplier in the config.

The amount of token = (enchent_level * multiplier) per interval

Installation:

Just install TE-TokenMinerEnchant.jar in TokenEnchant/enchants folder. TokenMiner enchantment will automatically be loaded into TokenEnchant framework.  You will also find the default config file (TokenMiner_config.yml) in the enchants folder.
