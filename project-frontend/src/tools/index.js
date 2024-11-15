function fitByUnit(value, unit ) {
    const units = ['B', 'KB', 'MB', 'GB', 'TB', 'PB']
    let index = units.indexOf(unit)
    while((value < 1 && value !== 0 || value > 1024) && (index >= 0 || index < units.length)) {
        if(value >= 1024 ) {
            value = value/1024
            index = index + 1
        } else {
            value = value * 1024
            index = index -1
        }
    }
    return `${parseInt(value)} ${units[index]}`
}

function percentageToStatus(percentage) {
    if(percentage < 50)
        return 'success'
    else if(percentage < 80)
        return 'warning'
    else
        return 'exception'
}

export  {fitByUnit, percentageToStatus}