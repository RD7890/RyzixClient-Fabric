# OreESP Performance Issue

## Problem Description
The `OreESP` module currently causes massive lag (TPS drops and FPS stuttering). 
This occurs because the `render` method in `OreESP.java` runs on the main render thread every single frame and manually iterates through every block in a 4-chunk radius below Y=64.

### The Math:
- 4-chunk radius = 9x9 grid = 81 chunks
- Each chunk scanned from Y=0 to 64 = 16 * 16 * 64 = 16,384 blocks per chunk
- 81 chunks * 16,384 blocks = **1,327,104 block checks per frame**
At 60 FPS, this translates to over 79 million `getBlockState()` checks per second on the main thread, choking the CPU (especially on PojavLauncher).

## Constraints Imposed
- Scan radius: Limit to exactly **1 chunk** (the chunk the player is currently standing in).
- Scan frequency: Scan only once every **3 seconds** (3000ms).
- Mechanism: Cache the found ores, and the `render` loop should only iterate over the cached coordinates.

## Proposed Fix Approach
1. **Caching Structure**:
   Create a thread-safe list (e.g., `CopyOnWriteArrayList<CachedOre>`) in `OreESP.java`. A `CachedOre` class/record will store the `BlockPos` and the ore type (or RGB colors) to draw.

2. **Async / Timed Scanning**:
   Use a `long lastScanTime = 0;` variable. Inside the `render` loop (or client tick), check if `System.currentTimeMillis() - lastScanTime >= 3000`. 
   If 3 seconds have passed:
   - Update `lastScanTime`.
   - Clear the cache.
   - Launch an asynchronous thread (`new Thread(() -> { ... }).start();` or a thread pool) to perform the block scanning to prevent halting the main render thread.

3. **Scanning Logic**:
   - The thread gets the player's current `ChunkPos`.
   - It iterates through X: 0-15, Z: 0-15, Y: 0-64 of *only that one chunk*.
   - If an iron, gold, lapis, or diamond ore is found, it adds a new `CachedOre` to the concurrent list.

4. **Rendering Logic**:
   - The `render` method in `OreESP` simply iterates over the `CopyOnWriteArrayList` of cached ores and draws the `RenderUtils.drawBox` for each one.
   - It checks the `showIron`, `showGold`, etc., toggles dynamically during rendering so turning off an ore hides it immediately without waiting for the next 3-second scan.

This will reduce the block checks from 1.3 million *per frame* to just 16,384 blocks *every 3 seconds on a background thread*, completely eliminating the lag.
