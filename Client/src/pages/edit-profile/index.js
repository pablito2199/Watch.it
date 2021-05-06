import { ArrowCircleLeftOutline as Back, FilmSolid as RatingIcon, CalendarOutline as Calendar, LocationMarkerOutline as Location, SaveOutline as Save } from '@graywolfai/react-heroicons'
import { useRef, useState } from 'react'
import { Input, Link, Shell } from '../../components'

import { useUser, useComments } from '../../hooks'

export default function EditProfile() {
    const { user, create, update } = useUser()
    const [picture, setPicture] = useState('')

    const submit = async (event) => {
        await update({
            name: user.name,
            country: user.country,
            picture: user.picture
        })
    }

    return <Shell>
        <div className='mx-auto w-full max-w-screen-2xl p-8'>
            <img
                style={{ height: '36rem' }}
                src={picture !== '' ? picture : user.picture}
                alt={user.name}
                className='absolute top-2 left-0 right-0 w-full object-cover filter blur transform scale-105'
            />

            <Link variant='primary'
                className='rounded-full absolute text-white top-4 left-8 flex items-center pl-2 pr-4 py-2 gap-4'
                to='/profile'
            >
                <Back className='w-8 h-8' />
                <span>Volver</span>
            </Link>

            <Link variant='primary'
                className='rounded-full absolute text-white top-4 right-8 flex items-center px-2 py-2 gap-4'
                to={`/profile`}
            >
                <Save
                    className='w-8 h-8'
                    onClick={submit}
                />
            </Link>

            <Header user={user} setPicture={setPicture} />
            <PageInfo />
        </div>
    </Shell>
}

function Header({ user, setPicture }) {
    const [visible, setVisible] = useState(false)

    return <header className='mt-96 relative flex pb-8 mb-8'>
        <img src={user.picture}
            alt={user.name}
            className='absolute w-64 rounded-full shadow-xl z-10 cursor-pointer'
            onClick={() => { setVisible(!visible) }}
            style={{ aspectRatio: '1/1' }} 
        />
        <Input
            labelClassName={`w-64 absolute self-center ${visible ? '' : 'hidden'} z-20`}
            defaultValue={user.picture}
            onChange={(event) => { 
                user.picture = event.target.value
                setPicture(event.target.value)
            }}
        />
        <hgroup className='ml-12 flex-1 mt-28'>
            <Input
                className={`bg-black bg-opacity-50 backdrop-filter backdrop-blur 
                                      text-right text-white text-6xl font-bold
                                      p-12`}
                defaultValue={user.name}
                onChange={(event) => user.name = (event.target.value)}
            />
            <Info user={user} />
        </hgroup>
    </header>
}
function Info({ user }) {
    return <div className='flex justify-between'>
        <div className='ml-60 flex'>
            <Calendar className='h-12 w-12 mt-2' />
            <span className={`self-centerblock text-2xl font-semibold text-black w-full py-4 text-right`}>
                {
                    user.birthday && <>{user.birthday.day}/{user.birthday.month}/{user.birthday.year}</>
                }
            </span>
        </div>
        <div className='flex ml-60'>
            <Location className='h-12 w-12 mt-2' />
            <Input
                className={`mt-2 text-2xl font-semibold text-black py-4`}
                defaultValue={user.country}
                onChange={(event) => user.country = (event.target.value)}
            />
        </div>
        <span className={`block text-3xl font-semibold text-black px-8 py-4 text-right`}>
            {user.email}
        </span>
    </div>
}
function PageInfo() {
    return <div className='text-gray-400 text-2xl text-center mt-16 mb-40'>
        <p>
            Para actualizar tu perfil simplemente coloca tu cursor encima de ella y
        </p>
        <p>
            modifícala a tu antojo.
        </p>
        <p>
            No podrás modificar tu dirección de correo electrónico ni tu fecha de nacimiento.
        </p>
    </div>
}