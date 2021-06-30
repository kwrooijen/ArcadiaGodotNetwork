(ns arcadia.network
  (:require
   [arcadia.core :as a]
   [clojure.edn :as edn])
  (:import
   [GD]))

(defmulti handler (fn [k _sender-id & _args] k))
(defmethod handler :default [k _sender-id & _args]
  (GD/Print (into-array ["Unhandled call: " (str k)])))

(defn handler_ [k sender-id args]
  (apply handler (keyword k) sender-id (edn/read-string args)))

(defn handler-fn []
  #'handler_)

(defn call! [k & args]
  (.Rpc (a/find-node "ArcadiaNetwork") "ArcadiaRPC" (to-array [(str (namespace k) "/" (name k)) (pr-str args)])))

(defn call-id! [id k & args]
  (.RpcId (a/find-node "ArcadiaNetwork") id "ArcadiaRPC" (to-array [(str (namespace k) "/" (name k)) (pr-str args)])))

(defn call-unreliable! [k & args]
  (.RpcUnreliable (a/find-node "ArcadiaNetwork") "ArcadiaRPC" (to-array [(str (namespace k) "/" (name k)) (pr-str args)])))

(defn call-unreliable-id! [id k & args]
  (.RpcUnreliableId (a/find-node "ArcadiaNetwork") id "ArcadiaRPC" (to-array [(str (namespace k) "/" (name k)) (pr-str args)])))

(def default-server-opts
  {:port 3000
   :max-clients 100
   :in-bandwidth 0
   :out-bandwidth 0
   :peer-connected nil
   :peer-disconnected nil})

(defn create-server
  "https://docs.godotengine.org/en/stable/classes/class_networkedmultiplayerenet.html#class-networkedmultiplayerenet-method-create-server"
  ([] (create-server default-server-opts))
  ([{:keys [port max-clients in-bandwidth out-bandwidth] :as opts}]
   (let [arcadia-network (new Godot.NetworkedMultiplayerENet)]
     (.CreateServer arcadia-network port max-clients in-bandwidth out-bandwidth)
     (.SetNetworkPeer (Godot.Engine/GetMainLoop) arcadia-network)
     (when-let [peer-connected (:peer-connected opts)]
       (a/connect arcadia-network "peer_connected" peer-connected))
     (when-let [peer-disconnected (:peer-disconnected opts)]
       (a/connect arcadia-network "peer_disconnected" peer-disconnected))
     arcadia-network)))

(def default-client-opts
  {:ip "127.0.0.1"
   :port 3000
   :client-port 0
   :in-bandwidth 0
   :out-bandwidth 0
   :connection-failed nil
   :connection-succeeded nil
   :server-disconnected nil})

(defn create-client
  "https://docs.godotengine.org/en/stable/classes/class_networkedmultiplayerenet.html#class-networkedmultiplayerenet-method-create-client"
  ([] (create-client default-client-opts))
  ([{:keys [ip port client-port in-bandwidth out-bandwidth] :as opts}]
   (let [arcadia-network (new Godot.NetworkedMultiplayerENet)]
     (.CreateClient arcadia-network ip port client-port in-bandwidth out-bandwidth)
     (.SetNetworkPeer (Godot.Engine/GetMainLoop) arcadia-network)
     (when-let [connection-failed  (:connection-failed opts)]
       (a/connect arcadia-network "connection_failed" connection-failed))
     (when-let [connection-succeeded (:connection-succeeded opts)]
       (a/connect arcadia-network "connection_succeeded" connection-succeeded))
     (when-let [server-disconnected (:server-disconnected opts)]
       (a/connect arcadia-network "server_disconnected" server-disconnected))
     arcadia-network)))

(defn client-tree-ready [self _]
  (create-client (a/state self)))

(defn server-tree-ready [self _]
  (create-server (a/state self)))

(defn add-client-node!
  ([] (add-client-node! default-client-opts))
  ([opts]
   (let [opts (merge default-client-opts opts)
         client-path "ArcadiaGodotNetwork/Source/client/ArcadiaNetwork.tscn"
         arcadia-network (-> client-path a/load-scene a/instance)]
     (a/set-state arcadia-network opts)
     (a/add-child (a/root) arcadia-network)
     arcadia-network)))

(defn add-server-node!
  ([] (add-server-node! default-client-opts))
  ([opts]
   (let [opts (merge default-server-opts opts)
         server-path "ArcadiaGodotNetwork/Source/server/ArcadiaNetwork.tscn"
         arcadia-network (-> server-path a/load-scene a/instance)]
     (a/set-state arcadia-network opts)
     (a/add-child (a/root) arcadia-network)
     arcadia-network)))
