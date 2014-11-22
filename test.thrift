namespace java me.vela.thrift.test.service

service TestThriftService {
    string echo(1:string message);
}