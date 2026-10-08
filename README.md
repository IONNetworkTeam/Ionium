<img src="branding/icon-512.png" width="128">

# Ionium

Ionium is Celeritas for Minecraft 1.8.9 Forge. [Celeritas](https://git.taumc.org/embeddedt/celeritas) is
embeddedt's fork of Embeddium, which goes back to the last open-source Sodium (0.5.11). It already ran on 1.8.9
through Ornithe, but nobody plays 1.8.9 on Ornithe, so we built a Forge version for the ION Network client.

We're not connected to CaffeineMC or the Celeritas project. If something breaks, open an issue here and please
don't bother them with it.

## Is it any good?

It's early, but it works and renders the same as vanilla as far as we've seen. On our test machine (render
distance 12, Forge on Java 21) it went from about 710 to 990 fps standing still and from 520 to 980 fps while
turning the camera. Lots of mobs on screen barely changes anything, because Ionium only speeds up terrain.

The same renderer does about 2,200 fps on Ornithe, so something on the Forge side still eats time every frame.
That's the next thing we're looking at. Right now OptiFine is still faster on Forge, and the two don't run
together anyway.

## What you need

This won't run on a normal Forge 1.8.9 install. Like Celeritas on other old versions it needs Java 21 and LWJGL 3,
so you need:

- Java 21
- [RetroFuturaBootstrap](https://github.com/GTNewHorizons/RetroFuturaBootstrap) in place of LaunchWrapper; we use
  [8to25](https://github.com/Oondanomala/8to25) for that
- LWJGL 3 from the launcher, plus the LWJGL 2 compatibility layer that ships with
  [ION Client](https://github.com/Juli0q/IONClientMod)

The [ION Launcher](https://launcher.ion-network.de) sets all of this up for you. If something is missing,
Ionium shows a message saying what's wrong when the game starts and then closes the game.

## Building

You need JDK 21.

```sh
./gradlew -Pceleritas_target_versions=1.8.9 :forge189:packageJar
```

The jar lands in `build/libs/<version>/`.

There's no Forge-specific code here. The `forge189` project takes the Ornithe 1.8.9 jar, remaps it from Ornithe's
Calamus names to Forge's SRG names (Mixin targets included, see `forge189/src/remapper`), bundles fastutil because
1.8.9 doesn't ship it, and writes a Forge manifest.

Changes compared to Celeritas:

- the Forge build in `forge189/`
- the mixin plugin and the entry point no longer need Fabric Loader. On Forge we skip two mixins: `MinecraftMixin`
  only fixes Beta 1.7.3 windows, and `ChunkCacheMixin` tracks the server's chunks, which only matters before 1.3
- F3 says "Ionium Renderer". Packages and the mod id are unchanged so we can keep merging from upstream
  (remote `upstream`, https://git.taumc.org/embeddedt/celeritas)
- our icon in `branding/`, drawn by `make_icon.py`

The original README is still there as [README.celeritas.md](README.celeritas.md).

## License

LGPL-3.0, same as Celeritas (`COPYING`, `COPYING.LESSER`). Thanks to embeddedt and everyone who worked on
Celeritas, Embeddium, Sodium up to 0.5.11 (CaffeineMC), Iris 1.7, Reese's Sodium Options (MIT) and bitraster.
The Forge jar also bundles fastutil (Apache-2.0) and JOML (MIT). Nothing here comes from Sodium 0.6 or later,
which isn't open source.
