# VelocityReport
Minecraft Report plugin for [Velocity](https://github.com/PaperMC/Velocity) servers with amazing features.  
**⚠️ Notice: Installing plugin on Velocity is optional. If you don't want to link report data between servers you can simply install plugin on all back-end (Spigot, Paper, etc...) servers**   
**⚠️ Notice: If you want to use `velocity_support` feature you should use MySQL as database method. otherwise report data will NOT sync between servers.**

## Commands:
[] = Optional
| Command | Permission| Description |
| --- | --- | --- |
| report <player> [reason] | velocityreport.report | Report a player |
| reportadmin reports | velocityreport.admin.reports | See all active reports |
| reportadmin myreports | velocityreport.admin.myreports | See all pending reports that you accepted |
| reportadmin accept [id] | velocityreport.admin.accept | Accept report |
| reportadmin done [id] | velocityreport.admin.done | Mark report as Done |
| reportadmin reload | velocityreport.admin.reload | Reload configuration files |

## Images
| ![](https://i.imgur.com/pDdXDo6.png) |
| --- |
| Report counter actionbar |
| ![](https://i.imgur.com/sMTUcMN.png) |
| Report Menu |
| ![](https://i.imgur.com/JqjPBmN.png) |
| Moderate reports menu |
| ![](https://i.imgur.com/XI1uGjb.png) |
| Accepted reports menu |

## Report system explained
### Database
VelocityReport supports both MySQL and SQLite databases.  
  
#### Database structure:
```sql
"report_id" VARCHAR(64) NOT NULL,
"reporter_id" VARCHAR(64) NULL,
"reporter_name" VARCHAR(16) NULL,
"reported_name" VARCHAR(16) NULL,
"date" BIGINT NULL,
"reason" VARCHAR(128) NULL,
"moderator_id" VARCHAR(64) NULL,
"server" VARCHAR(64) NULL,
"moderator_name" VARCHAR(16) NULL,
"stage" VARCHAR(64) NULL,
PRIMARY KEY ("report_id")
```  
#### Storage yaml file
```yml
type: SQLite
mysql:
    address: localhost
    port: 3306
    database: velocityreport
    username: root
    password: ""
    ssl: false
    pooling_size: 3
```

### Stages:
Every report have three stage:
- **ACTIVE**: The default stage for every report (You can see active reports via `/reportadmin reports`)
- **PENDING**: When someone accepted the report (You can see the reports that you accepted in `/reportadmin myreports` menu)
- **DONE**: When someone mark report as resolved (All resolved reports will save in Database with DONE stage)

### Customize:   
Almost everything is customizable via yaml files.   
**All messages support Gradient/RGB and TextComponent via [MiniMessage](https://docs.adventure.kyori.net/minimessage/) format**

#### Settings yaml file:
```yml
default_language: en_US
velocity_support: false
report:
    cooldown: 60
    staff_actionbar: true
    prevent_self: true
    custom_reason: false
    reasons:
        killaura:
            enabled: true
            displayname: "<blue>Killaura"
            description: "Killaura is a cheat that allows you to attack players without looking at them."
        reach:
            enabled: true
            displayname: "<blue>Reach"
            description: "Reach is a cheat that allows you to attack players from a long distance."
        speed:
            enabled: true
            displayname: "<blue>Speed"
            description: "Speed is a cheat that allows you to move faster than normal."
        scaffold:
            enabled: true
            displayname: "<blue>Scaffold"
            description: "Scaffold is a cheat that allows you to place blocks under you."
        fly:
            enabled: true
            displayname: "<blue>Fly"
            description: "Fly is a cheat that allows you to fly in the air."
        aimbot:
            enabled: true
            displayname: "<blue>Aimbot"
            description: "Aimbot is a cheat that allows you to aim at players automatically."
        esp:
            enabled: true
            displayname: "<blue>ESP"
            description: "ESP is a cheat that allows you to see players through walls."
```

#### Default language yaml file
* You can add your custom language yaml file in `languages` folder
* Default language is en_US
```yml
general:
    raw_prefix: "[VelocityReport]"
    prefix: "<gradient:dark_red:red>VelocityReport</gradient> <gold>|</gold>"
    console_prefix: "<gray>[<gradient:dark_red:red>VelocityReport</gradient>]</gray>"
    successful_prefix: "<dark_gray>[</dark_gray><dark_green><bold>✔</bold><dark_gray>]</dark_gray>"
    warn_prefix: "<dark_gray>[</dark_gray><gold><bold>!</bold><dark_gray>]</dark_gray>"
    error_prefix: "<dark_gray>[</dark_gray><dark_red><bold>✘</bold><dark_gray>]</dark_gray>"
    only_players: "$error_prefix <gradient:dark_red:red>Only players can use this command."
    valid_parameters: "$error_prefix <gradient:dark_red:red>Please use a valid parameter for this command. <dark_gray>($argument)"
    unknown_message: "$error_prefix <gradient:dark_red:red>Unknown message!"
command:
    no_permission: "$error_prefix <gradient:dark_red:red>You don't have permission to use this command! <dark_gray>($permission)"
    report:
        usage: "$warn_prefix <gradient:dark_red:red>Usage: <gold>/report <yellow><user> [reason]"
        use: "$successful_prefix <gradient:dark_green:green>$player successfully reported for <aqua>$reason<aqua>!"
        no_target: "$error_prefix <gradient:dark_red:red>Player not found!"
        prevent_self: "$error_prefix <gradient:dark_red:red>You can't report yourself!"
        cooldown: "$error_prefix <gradient:dark_red:red>You can't use this command for <gold>$time</gold> seconds!"
        invalid_reason: "$error_prefix <gradient:dark_red:red><gold>$reason</gold> is not an invalid reason!"
        book:
            header:
                - "<dark_red>❐ <gradient:dark_red:red>Report Reason:"
                - ""
                - ""
            reason: "<click:run_command:'/report $player $id'><hover:show_text:'<blue>Description: $description \n\n <yellow>Click to report!'><gold>● <dark_blue>$name</hover></click>"
            footer:
                - ""
    reportadmin:
        usage: "$warn_prefix <gradient:dark_red:red>Usage: <gold>/reportadmin <yellow><args>"
        reload:
            use: "$successful_prefix <green>Plugin successfully reloaded!"
        reports:
            receive: "$warn_prefix <gradient:gold:yellow>Receiving reports data from database..."
            book:
                header:
                    - "<dark_red>❐ <gradient:dark_red:red>Reports:"
                    - ""
                format: "<click:run_command:'/reportadmin accept $id'><hover:show_text:'<blue>Reporter: <aqua>$reporter</aqua>\nReported: <aqua>$reported</aqua>\nServer: <aqua>$server</aqua>\n\nReason: <aqua>$reason</aqua>\n\n <yellow>Click to follow up report!'><gold>● <dark_blue>$reported</hover></click>"
        accept:
            usage: "$warn_prefix <gradient:dark_red:red>Usage: <gold>/reportadmin accept <yellow><report id>"
            use: "$successful_prefix <gradient:dark_green:green>Report successfully accepted ($id)!"
            already_accepted: "$warn_prefix <gradient:red:gold>This report doesn't exist or already accepted ($id)!"
        done:
            usage: "$warn_prefix <gradient:dark_red:red>Usage: <gold>/reportadmin done <yellow><report id>"
            use: "$successful_prefix <gradient:dark_green:green>Report successfully done ($id)!"
            already_done: "$warn_prefix <gradient:red:gold>This report doesn't exist or already done ($id)!"
        myreports:
            receive: "$warn_prefix <gradient:gold:yellow>Receiving your reports data from database..."
            use: "$successful_prefix <gradient:dark_green:green>Report successfully done ($id)!"
            book:
                header:
                    - "<dark_red>❐ <gradient:dark_red:red>Your reports:"
                    - ""
                format: "<click:run_command:'/reportadmin done $id'><hover:show_text:'<blue>Reporter: <aqua>$reporter</aqua>\nReported: <aqua>$reported</aqua>\nServer: <aqua>$server</aqua>\n\nReason: <aqua>$reason</aqua>\n\n <yellow>Click to mark as done!'><gold>● <dark_blue>$reported</hover></click>"

report:
    actionbar: "<gold>⚠ <gradient:dark_purple:blue>There are <gold>$reports</gold> reports!"
```
