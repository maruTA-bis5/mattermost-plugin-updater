# Mattermost Plugin Updater

Mattermost Plugin Updater is a Java application for update [Mattermost](https://mattermost.com) plugins.

## Prerequisites

- Runtime: Java SE 11
- Mattermost Server: 5.8 or later

## Usage

1. Download plugin-updater-x.y.z.jar from release archive.
2. Create plugin definision file (plugin.yml) for the plugin to update.
3. Run `java -DmattermostUrl=http://your-mattermost-server -Dusername=<username> -Dpassword=<password> -jar plugin-updater-x.y.z.jar /path/to/plugin.yml`.
    - If you want to use the Personal Access Token to upload plugin, replace `-Dusername`/`-Dpassword` with`-DaccessToken=<your access token>`

### Example of `plugin.yml`
```yaml
name: Sample Plugin
version: 0.1.0
archiveUrl: https://github.com/maruTA-bis5/mattermost-plugin-updater/releases/download/0.1.0/sample-plugin.tar.gz
releaseNoteUrl: https://github.com/maruTA-bis5/mattermost-plugin-updater/releases/tag/0.1.0
updatePostTemplate: |
    #### ${name} Updated to version ${version}.
    Release note: ${releaseNoteUrl}
postWebhooks:
    - https://your-mattermost-server/hooks/incomingwebhook-for-team-A
    - https://your-mattermost-server/hooks/incomingwebhook-for-team-B
postUsername: Plugin Update
postIcon: https://host/path/to/image.png
```

## Contributing

Pull requests are welcome. For major changes, please open an issue first to discuss what you would like to change.

## License

[Apache License, Version 2.0](https://www.apache.org/licenses/LICENSE-2.0)
