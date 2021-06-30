# ArcadiaGodotNetwork

This tool is heavily in development and uses premature solutions to solve its problems. It is therefore not recommended to use in production.


## Usage

It is good to understand the basics of [Godot Networking](https://docs.godotengine.org/en/stable/tutorials/networking/high_level_multiplayer.html).

### Client

Create a client namespace in your Godot Client project. Create a client node with the `arcadia.network/add-client-node!`, this should be called in your main node's `tree-ready` function.

```clojure
(ns my-project.client
  (:require
   [arcadia.network :as network]))

(defn connection-failed []
  (a/log "Connection failed!"))

(defn connection-succeeded []
  (a/log "Connection succeeded!"))

(defn server-disconnected []
  (a/log "Server disconnected"))

(defn start! []
  (network/add-client-node!
   {:connection-failed connection-failed
    :connection-succeeded connection-succeeded
    :server-disconnected server-disconnected}))
```

Default Client opts:

```clojure
(def default-client-opts
  {:ip "127.0.0.1"
   :port 3000
   :client-port 0
   :in-bandwidth 0
   :out-bandwidth 0
   :connection-failed nil
   :connection-succeeded nil
   :server-disconnected nil})
```

### Server

In your server's main scene you can call the `arcadia.network/add-server-node!`
function to add the ArcadiaNetwork node to your game.


```clojure
(ns my-project.server
  (:require
   [arcadia.network :as network]
   [arcadia.core :as a]))

(defn peer-connected [client-id]
  (a/log "client" client-id "connected"))

(defn peer-disconnected [player-id]
  (a/log "User" player-id "disconnected"))

(defn ^{:hook/tree-ready ["res://Scenes/Main.tscn"]} ready-tree [self _]
  (network/add-server-node!
   {:peer-connected peer-connected
    :peer-disconnected peer-disconnected}))

```

Default Server opts:

```clojure
(def default-server-opts
  {:port 3000
   :max-clients 100
   :in-bandwidth 0
   :out-bandwidth 0
   :peer-connected nil
   :peer-disconnected nil})
```

### Communication


After your server and client are setup, they can start communication using
handlers and RPC calls. Handlers are defined using the `arcadia.network/handler`
multimethod. Which takes a keyword (identifier), sender-id, and rest arguments.

```clojure
(defmethod arcadia.network/handler :server/handler [_k sender-id & args]
  (a/log "Got :" args " from: " sender-id))
```

Next you can call these handlers using `arcadia.network/rpc!` and `arcadia.network/rpc-id!`

```clojure
;; rpc! == all connected clients
(arcadia.network/rpc! :server/handler "Hello" "world!")

;; 1 == server
(arcadia.network/rpc-id! :server/call 1 "Hello" "world!")

;; rpc! == all connected clients
(arcadia.network/rpc-unreliable! :server/handler "Hello" "world!")

;; 1 == server
(arcadia.network/rpc-unreliable-id! :server/call 1 "Hello" "world!")
```

## Author / License

Released under the [MIT License] by [Kevin William van Rooijen].

[Kevin William van Rooijen]: https://twitter.com/kwrooijen

[MIT License]: https://github.com/kwrooijen/ArcadiaGodotNetwork/blob/master/LICENSE
