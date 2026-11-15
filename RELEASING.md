


## Warming up the GPG agent for signing during the Maven release:

```shell
export GPG_TTY="$(tty)"
gpgconf --kill gpg-agent
gpgconf --launch gpg-agent
gpg-connect-agent updatestartuptty /bye

printf 'KBake GPG agent check\n' |   gpg --local-user '...@...m.cz'       --detach-sign --output /dev/null

## Try:
mvn -N -Prelease org.apache.maven.plugins:maven-gpg-plugin:3.2.8:sign
```
