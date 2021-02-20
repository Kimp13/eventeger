package ru.labore.moderngymnasium.data.network.exceptions

import java.io.IOException

class ClientErrorException(val errorCode: Int) : IOException()