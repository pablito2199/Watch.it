import * as jsonpatch from 'fast-json-patch/index.mjs';
import { applyOperation } from 'fast-json-patch/index.mjs';

let __instance = null

export default class API {
    #token = localStorage.getItem('token') || null

    static instance() {
        if (__instance == null)
            __instance = new API()

        return __instance
    }

    async login(email, pass) {
        const requestOptions = {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ email: email, password: pass })
        };

        const response = await fetch(`http://localhost:8080/login`, requestOptions);

        if (response.status === 200) {
            localStorage.setItem('user', email)
            localStorage.setItem('token', response.headers.get("Authentication"))
            this.#token = response.headers.get("Authentication")
            return true
        } else {
            return false
        }
    }

    async logout() {
        this.#token = null
        localStorage.clear()

        return true
    }

    async findMovies(
        {
            filter: { genre = '', title = '', status = '' } = { genre: '', title: '', status: '' },
            sort,
            pagination: { page = 0, size = 7 } = { page: 0, size: 7 }
        } = {
                filter: { genre: '', title: '', status: '' },
                sort: {},
                pagination: { page: 0, size: 7 }
            }
    ) {
        const requestOptions = {
            method: "GET",
            headers: {
                "Content-Type": "application/json",
                "Authorization": this.#token
            }
        };

        let parameters = `?page=${page}&size=${size}`
        if (genre !== '') {
            parameters += `&genres=${genre}`
        }
        if (title !== '') {
            parameters += `&keywords=${title}`
        }
        if (status !== '') {
            parameters += `&status=${status}`
        }
        for (let key in sort) {
            sort[key] === 'ASC' ? parameters += `&sort=+${key}` : parameters += `&sort=-${key}`
        }

        const response = await fetch(`http://localhost:8080/films${parameters}`, requestOptions);
        if (response.status === 200) {
            const movieData = await response.json()
            return {
                content: movieData.content,
                pagination: {
                    hasNext: !movieData.last,
                    hasPrevious: !movieData.first
                } 
            }
        }
    }

    async findMovie(id) {
        const requestOptions = {
            method: "GET",
            headers: {
                "Content-Type": "application/json",
                "Authorization": this.#token
            }
        };

        const response = await fetch(`http://localhost:8080/films/${id}`, requestOptions);

        if (response.status === 200) {
            return await response.json()
        }
    }

    async findUser(id) {
        const requestOptions = {
            method: "GET",
            headers: {
                "Content-Type": "application/json",
                "Authorization": this.#token
            }
        };

        const response = await fetch(`http://localhost:8080/users/${id}`, requestOptions);

        if (response.status === 200) {
            return await response.json()
        }
    }

    async findFriendships(userId) {
        const requestOptions = {
            method: "GET",
            headers: {
                "Content-Type": "application/json",
                "Authorization": this.#token
            }
        };

        const response = await fetch(`http://localhost:8080/users/${userId}/friendships`, requestOptions);
        
        if (response.status === 200) {
            return await response.json()
        }
    }

    async findComments(
        {
            filter: { movie = '', user = '' } = { movie: '', user: '' },
            sort,
            pagination: { page = 0, size = 10 } = { page: 0, size: 10 }
        } = {
                filter: { movie: '', user: '' },
                sort: {},
                pagination: { page: 0, size: 10 }
            }
    ) {
        const requestOptions = {
            method: "GET",
            headers: {
                "Content-Type": "application/json",
                "Authorization": this.#token
            }
        };

        let filter
        movie !== '' ? filter = `films/${movie}/` : filter = `users/${user}/`
        let parameters = `?page=${page}&size=${size}`
        for (let key in sort) {
            sort[key] === 'ASC' ? parameters += `&sort=+${key}` : parameters += `&sort=-${key}`
        }

        const response = await fetch(`http://localhost:8080/${filter}assessments${parameters}`, requestOptions);

        if (response.status === 200) {
            return await response.json()
        }
    }

    async createComment(assessment) {
        const requestOptions = {
            method: 'POST',
            headers: {
                "Content-Type": "application/json",
                "Authorization": this.#token
            },
            body: JSON.stringify({
                rating: assessment.rating,
                user: {
                    email: assessment.user
                },
                film: {
                    id: assessment.film
                },
                comment: assessment.comment
            })
        };

        const response = await fetch(`http://localhost:8080/films/assessments`, requestOptions);

        if (response.status === 200) {
            return true
        } else {
            return false
        }
    }

    async createUser(user) {
        const requestOptions = {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({
                email: user.email,
                name: user.name,
                password: user.password,
                country: "Undefined",
                picture: "https://preview.redd.it/nx4jf8ry1fy51.gif?format=png8&s=a5d51e9aa6b4776ca94ebe30c9bb7a5aaaa265a6",
                birthday: {
                    day: user.birthday.day,
                    month: user.birthday.month,
                    year: user.birthday.year
                },
                roles: ["ROLE_USER"]
            })
        };

        const response = await fetch(`http://localhost:8080/users`, requestOptions);

        if (response.status === 200) {
            return true
        } else {
            return false
        }
    }

    async updateUser(id, user) {
        var diff = jsonpatch.compare(await this.findUser(id), user.user);

        const requestOptions = {
            method: 'PATCH',
            headers: {
                'Content-Type': 'application/json',
                "Authorization": this.#token
            },
            body: JSON.stringify(diff)
        };

        const response = await fetch(`http://localhost:8080/users/${id}`, requestOptions);

        if (response.status === 200) {
            return true
        } else {
            return false
        }
    }

    async updateMovie(id, movie) {
        var diff = jsonpatch.compare(await this.findMovie(id), movie.movie);

        const requestOptions = {
            method: 'PATCH',
            headers: {
                'Content-Type': 'application/json',
                "Authorization": this.#token
            },
            body: JSON.stringify(diff)
        };

        const response = await fetch(`http://localhost:8080/films/${id}`, requestOptions);

        if (response.status === 200) {
            return true
        } else {
            return false
        }
    }

    async updateFriendship(user, friend) {
        const requestOptions = {
            method: 'PUT',
            headers: {
                'Content-Type': 'application/json',
                "Authorization": this.#token
            }
        };

        console.log(user)
        console.log(friend)

        const response = await fetch(`http://localhost:8080/users/${user}/friendships/${friend}`, requestOptions);

        if (response.status === 200) {
            return true
        } else {
            return false
        }
    }

    async deleteFriend(user, friend) {
        const requestOptions = {
            method: 'DELETE',
            headers: {
                "Content-Type": "application/json",
                "Authorization": this.#token
            }
        };

        const response = await fetch(`http://localhost:8080/users/${user}/friendships/${friend}`, requestOptions);

        if (response.status === 200) {
            return true
        } else {
            return false
        }
    }
}