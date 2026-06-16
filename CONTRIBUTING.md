## Understanding Patches
Patches are very simple, but center around the directory `ScalableLux-Patched`

Assuming you already have forked the repository:

1. Pull the latest changes from the main repository
2. Type `./build.sh patch` in git bash to apply the changes from upstream
3. cd into corresponding directories for changes

These directories aren't git repositories in the traditional sense:

- Every single commit is a patch.
- 'origin/master' points to upstream
- Typing `git status` should show that we are 10 or 11 commits ahead of master, meaning we have 10 or 11 patches that  doesn't
    - If it says something like `212 commits ahead, 207 commits behind`, then type `git fetch` to update 

## Adding Patches
Adding patches is very simple:

1. Make changes
2. `cd` into the directory you want to add a patch to
3. Type `git add .` to add your changes
4. Run `git commit` with the desired patch message
5. `cd ../` to get back to the project root
6. Run `./build.sh rebuild` in the main directory to convert your commit into a new patch
7. PR your patches back to this repository

Your commit will be converted into a patch that you can then PR back to this repository.

## Modifying Patches
Modifying previous patches is a bit more complex:

### Method 1
This method works by temporarily resetting HEAD to the desired commit to edit using rebase.

1. If you have changes you are working on type `git stash` to store them for later.
    - Later you can type `git stash pop` to get them back.
2. Type `git rebase -i upstream/upstream`
    - It should show something like [this](https://gist.github.com/zachbr/21e92993cb99f62ffd7905d7b02f3159).
3. Replace `pick` with `edit` for the commit/patch you want to modify, and "save" the changes.
    - Only do this for one commit at a time.
4. Make the changes you want to make to the patch.
5. Type `git add .` to add your changes.
6. Type `git commit --amend` to commit.
    - **MAKE SURE TO ADD `--amend`** or else a new patch will be created.
    - You can also modify the commit message here.
7. Type `git rebase --continue` to finish rebasing.
8. Type `./build.sh rebuild` in the main directory.
    - This will modify the appropriate patches based on your commits.
9. PR your modifications back to this project.

### Method 2 (sometimes easier)
If you are simply editing a more recent commit or your change is small, simply making the change at HEAD and then moving the commit after you have tested it may be easier.

This method has the benefit of being able to compile to test your change without messing with your API HEAD.

1. Make your change while at HEAD
2. Make a temporary commit. You don't need to make a message for this.
3. Type `git rebase -i upstream/upstream`, move (cut) your temporary commit and move it under the line of the patch you wish to modify.
4. Change the `pick` with `f` (fixup) or `s` (squash) if you need to edit the commit message
5. Type `./build.sh rebuild` in the main directory
    - This will modify the appropriate patches based on your commits
6. PR your modifications back to this project.


## Rebasing PRs

Steps to rebase a PR to include the latest changes from `master`.  
These steps assume the `origin` remote is your fork of this repository and `upstream` is the upstream repository.

1. Pull latest changes from upstream's master: `git checkout master && git pull upstream master`.
2. Checkout feature/fix branch and rebase on master: `git checkout patch-branch && git rebase master`.
3. Apply updated patches: `./build.sh patch`.
4. If there are conflicts, fix them.
5. If your PR creates new patches instead of modifying exist ones, ensure your newly-created patch is the last commit by either:
    * Renaming the patch file with a large 4 digit number in front (e.g. 9999-Patch-to-add-some-new-stuff.patch)
    * Run `git rebase -i upstream/upstream` and move the commits to the end.
6. Rebuild patches: `./build.sh rebuild`.
7. Force push changes: `git push --force`.

## Patch Notes
When submitting patches, we may ask you to add notes to the patch header.
While we do not require it for all changes, you should add patch notes when the changes you're making are technical or complex.
It is very likely that your patch will remain long after we've all forgotten about the details of your PR, patch notes will help
us maintain it without having to dig back through GitHub history looking for your PR.

These notes should express the intent of your patch, as well as any pertinent technical details we should keep in mind long-term.
Ultimately, they exist to make it easier for us to maintain the patch across major version changes.

If you add a long message to your commit in the corresponding directory, the command will handle these patch
notes automatically as part of generating the patch file. Otherwise, if you're careful they can be added by hand 
(though you should be careful when doing this, and run it through a patch and rebuild cycle once or twice).

```patch
From 02abc033533f70ef3165a97bfda3f5c2fa58633a Mon Sep 17 00:00:00 2001
From: Shane Freeder <theboyetronic@gmail.com>
Date: Sun, 15 Oct 2017 00:29:07 +0100
Subject: [PATCH] revert serverside behavior of keepalives

This patch intends to bump up the time that a client has to reply to the
server back to 30 seconds as per pre 1.12.2, which allowed clients
more than enough time to reply potentially allowing them to be less
tempermental due to lag spikes on the network thread, e.g. that caused
by plugins that are interacting with netty.

We also add a system property to allow people to tweak how long the server
will wait for a reply. There is a compromise here between lower and higher
values, lower values will mean that dead connections can be closed sooner,
whereas higher values will make this less sensitive to issues such as spikes
from networking or during connections flood of chunk packets on slower clients,
 at the cost of dead connections being kept open for longer.

diff --git a/src/main/java/net/minecraft/server/PlayerConnection.java b/src/main/java/net/minecraft/server/PlayerConnection.java
index a92bf8967..d0ab87d0f 100644
--- a/src/main/java/net/minecraft/server/PlayerConnection.java
+++ b/src/main/java/net/minecraft/server/PlayerConnection.java
```

