using Godot;
using System;
using clojure.lang;

public class ArcadiaNetwork : Node
{
    [Remote]
    public void ArcadiaRPC(string keyword, string args){
        try{
            IFn fn = (IFn)Arcadia.Util.Invoke(RT.var("arcadia.network", "handler-fn"));
            int SenderId = GetTree().GetRpcSenderId();
            fn.invoke(keyword, SenderId, args);
        } catch (Exception err){
            GD.PrintErr(err);
        }
    }
}
