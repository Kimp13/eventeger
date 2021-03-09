package ru.labore.eventeger.data.network.exceptions

import java.io.IOException

class ClientErrorException(val errorCode: Int) : IOException()