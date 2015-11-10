# yetibot-stackstorm

Add yetibot-stackstorm as a dependency to your yetibot installation and
configure it.

This is a yetibot plugin. For more information on yetibot and how to use
plugins, see [yetibot](https://github.com/devth/yetibot).

[![Clojars Project](http://clojars.org/yetibot-stackstorm/latest-version.svg)](http://clojars.org/yetibot-stackstorm)

## Config

```edn
;; merge this into config/config.edn in your yetibot installation
{:yetibot-stackstorm
 {:models
  {:stackstorm
   {:api-key ""
    :api-endpoint ""}}}}
```

### Authorization

Because a Yetibot-StackStorm bridge opens up all kinds of power, a way to
specify authorized users is available at the config level using an `:authorized`
key and set of user IDs value. If this key is not present, no authorization
checking will occur. If it's an empty set, no users will be authorizaed to run
StackStorm aliases.

IDs in Slack look like `"U12341234"` and in IRC they look like `"~devth"`.

```edn
;; merge this into config/config.edn in your yetibot installation
{:yetibot-stackstorm
 {:models
  {:stackstorm
   {:authorized #{"~devth"}
    :api-key ""
    :api-endpoint ""}}}}
```



## License

Copyright © 2015 Trevor C. Hartman

Distributed under the Eclipse Public License version 1.0.
