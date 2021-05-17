import { useEffect, useState } from 'react'

import API from '../api'

export function useMovies(query = {}) {
    const [data, setData] = useState({ content: [], pagination: { hasNext: false, hasPrevious: false }})
    const queryString = JSON.stringify(query)

    useEffect(() => {
        API.instance()
            .findMovies(JSON.parse(queryString))
            .then(setData)
    }, [queryString])

    return data
}

export function useMovie(id = '') {
    const [data, setData] = useState({})
    const movieId = id

    useEffect(() => {
        API.instance()
            .findMovie(id)
            .then(movie => {
                setData(movie)
            })
    }, [movieId])

    const update = movie => {API.instance()
        .updateMovie(id, movie)
        .then(movie => setData(movie))} 

    return {
        movie: data,
        update
    }
}

export function useUser(id = null) {
    const [data, setData] = useState([])
    const userId = id === null ? id = localStorage.getItem('user') : id

    useEffect(() => {
        API.instance()
            .findUser(userId)
            .then(user => {
                setData(user)
            })
    }, [userId])

    const create = user => API.instance()
            .createUser(user)
            .then(user => setData(user))

    const update = user => API.instance()
            .updateUser(id, user)
            .then(updated => {return updated})

    return {
        user: data,
        create,
        update
    }
}

export function useFriends(id = null) {
    const [data, setData] = useState([])
    const userId = id === null ? id = localStorage.getItem('user') : id

    useEffect(() => {
        API.instance()
            .findFriendships(userId)
            .then(friends => {
                setData(friends)
            })
    }, [userId])

    /*const update = user => API.instance()
            .updateUser(id, user)
            .then(user => setData(user))*/

    return {
        friends: data,
        //update
    }
}

export function useComments(query = {}){
    const [data, setData] = useState({ content: [], pagination: { hasNext: false, hasPrevious: false }})
    const queryString = JSON.stringify(query)

    useEffect(() => {
        API.instance()
            .findComments(JSON.parse(queryString))
            .then(setData)
    }, [queryString])

    const create = comment => {
        API.instance()
            .createComment(comment)
            .then( () => {
                API.instance()
                    .findComments(query)
                    .then(setData)
            })
    }

    return {
        comments: data,
        createComment: create
    }
}