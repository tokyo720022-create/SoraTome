# Signing key

Android only installs an update over an installed app if both builds are signed with the **same key**. GitHub Actions builds on a fresh machine every time, so without a saved key every build gets a different temporary key, and you have to uninstall the old app first.

SoraTome fixes this by signing every build with one key kept in **GitHub Actions secrets**. The key is never stored in the repository, which is public.

## One-time setup

1. Create a keystore (a file holding the key) on your own computer or phone, not in the repository:

   ```
   keytool -genkeypair -J-Dkeystore.pkcs12.legacy -keystore soratome.keystore -storetype PKCS12 \
     -alias soratome -keyalg RSA -keysize 2048 -validity 10000 -dname "CN=SoraTome"
   ```

   Use the same password for the store and the key. The alias must be `soratome`.

2. Turn the file into one line of text: `base64 -w0 soratome.keystore`.
3. In the repository open **Settings > Secrets and variables > Actions > New repository secret** and add:

   | Secret name | Value |
   |---|---|
   | `SORATOME_KEYSTORE_BASE64` | the one line from step 2 |
   | `SORATOME_KEYSTORE_PASSWORD` | the keystore password |

4. Replace `.github/workflows/build.yml` with the current one from this project (it restores the key before building).
5. Uninstall the app **once** (the old builds used a temporary key), then install the next build. After that, new builds install over the top and keep your data.

## Notes

- Keep a private backup of the keystore and its password. If you lose them you can never update an installed app again; you would have to uninstall and reinstall.
- Do not commit the keystore. `.gitignore` already skips `*.keystore`, `*.jks`, `*.p12` and `*.b64`.
- Builds from forks or pull requests from other people do not get the secrets, so they use a temporary key. That is expected.
- The APK files published as workflow artifacts are public in a public repository. That is fine: the APK holds the public half of the key, never the private half.
- Each build's version code is the workflow run number, so a newer build always counts as newer.
- A key used for testing is not the one to use for a public release. Make a separate key for that and keep it offline.
