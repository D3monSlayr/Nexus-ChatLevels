# 💬 NexusChatLevels

A lightweight and customizable Minecraft chat leveling system built for Paper servers.  
Players gain XP through chatting and level up to unlock rewards — items or commands!

---

## ⚙️ Features
- Smooth XP & level tracking
- Material and command-based rewards
- Fully configurable leveling system
- Reloadable via command
- Permissions support
- Built for **Paper 1.20+**

---

## Commands

| Command | Description | Permission |
|----------|--------------|-------------|
| `/chatlvls help` | Shows the help menu | `chatlvls.use` |
| `/chatlvls xp` | Shows your current chat XP | `chatlvls.use` |
| `/chatlvls xp for <level>` | Shows XP required for a level | `chatlvls.use` |
| `/chatlvls xp player <player>` | Shows XP of another player | `chatlvls.admin` |
| `/chatlvls level` | Shows your current chat level | `chatlvls.use` |
| `/chatlvls level for <player>` | Shows another player’s level | `chatlvls.admin` |
| `/chatlvls reload` | Reloads configs and rewards | `chatlvls.admin` |
| `/chatlvls admin set xp <player> <xp>` | Sets player XP | `chatlvls.admin` |
| `/chatlvls admin set level <player> <level>` | Sets player level | `chatlvls.admin` |
| `/chatlvls admin rewards add material <level> <material:amount>` | Adds item reward | `chatlvls.admin` |
| `/chatlvls admin rewards add command <level> <command>` | Adds command reward | `chatlvls.admin` |
| `/chatlvls admin rewards remove <level> <reward>` | Removes reward | `chatlvls.admin` |
| `/chatlvls admin rewards get <level>` | Shows rewards for a level | `chatlvls.admin` |

Alias: `/chatlevels` → same as `/chatlvls`

---

## 🧠 Permissions
| Permission | Description |
|-------------|--------------|
| `chatlvls.use` | Basic command access |
| `chatlvls.admin` | Full admin and config access |

---

## 🧩 Installation
1. Download the latest release from the [Releases](../../releases) page.  
2. Drop the `.jar` file into your `plugins` folder.  
3. Restart your Paper server.  
4. Configure XP, levels, and rewards to your liking.

---

## 🧰 Requirements
- PaperMC 1.20+
- Java 17 or higher

---

## 🛠 Developer Info
- **Language:** Java  
- **Build Tool:** Maven  
- **Main Class:** `me.kythera.chatLevels.ChatLevels`

---

## License
This project is protected by a [**custom license**](LICENSE).  
You may view or fork the code, but **redistribution, modification, or commercial use** is **not allowed** without permission from the author.

© 2025 Kythera — All Rights Reserved.
