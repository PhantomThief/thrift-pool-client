namespace java com.github.phantomthief.thrift.test.service

service TestThriftService {
    string echo(1:string message);
}