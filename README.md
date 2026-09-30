# RyzixClient

Client-side Fabric mod for **Minecraft 1.20.4** (Fabric Loader 0.15.11+, Java 17).

## Modules
- **StorageESP** - highlights chests, barrels, shulkers, hoppers, furnaces etc.
- **PlayerESP** - boxes around other players
- **OreESP** - iron / gold / lapis / diamond (incl. deepslate variants), scans your chunk every 3s off-thread
- **FullBright** - client-side Night Vision effect (no gamma hack, nothing saved to options.txt)
- **Chest Counter** - HUD with the count of loaded storage containers

## Keys
- `R` - open mod menu
- `G` - toggle StorageESP
- `Z` - toggle OreESP
- PlayerESP / FullBright keys are unbound by default (Controls > RyzixClient)

## Build
`./gradlew build` - jar is in `build/libs/`. GitHub Actions builds and releases on every push to `main`.

## License
CC0-1.0
